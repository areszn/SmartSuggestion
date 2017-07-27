package zzli.Dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.CancellableThreads;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Repository;
import zzli.Config.MyConfig;
import zzli.Model.Action;
import zzli.Model.CommonContent;
import zzli.Model.CommonContentId;
import zzli.Model.UserKeywords;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by catfish on 2016-12-19.
 */
@Repository
public class ElasticTemplate {
    @Autowired
    TransportClient transportClient;
    @Autowired
    MyConfig myConfig;

    @PostConstruct
    void init(){
        String index=myConfig.recByContentIndex+1;
        try {
            if(transportClient.admin().indices().prepareExists(index).get().isExists()) return;
            XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                    .startObject(CommonContent.class.getSimpleName())
                    .startObject("properties")
                    .startObject("text")
                    .field("type", "text")
                    .field("analyzer", "ik_smart")
                    .field("search_analyzer", "ik_smart")
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();
            transportClient.admin().indices().prepareCreate(index)
                    .addMapping(CommonContent.class.getSimpleName(),content)
                    .get();

            content = XContentFactory.jsonBuilder().startObject()
                    .startObject(UserKeywords.class.getSimpleName())
                    .startObject("properties")
                    .startObject("keywords")
                    .field("type", "text")
                    .field("analyzer", "ik_smart")
                    .field("search_analyzer", "ik_smart")
                    .endObject()
                    .endObject()
                    .endObject()
                    .endObject();
            transportClient.admin().indices().preparePutMapping().setIndices(index)
                    .setType(UserKeywords.class.getSimpleName())
                    .setSource(content)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(String index, @Nullable String type,@Nullable String id, Object object){
        try {
            if(type==null){
                type=object.getClass().getSimpleName();
            }
            String js=new ObjectMapper().writeValueAsString(object);
            if(id==null){
                transportClient.prepareIndex(index,type).setSource(js).get();
            }else {
                GetResponse getResponse=null;
                try {
                    getResponse=transportClient.prepareGet(index,type,id).get();
                }catch (Exception e){e.printStackTrace();}

                if(getResponse==null||!getResponse.isExists()){
                    transportClient.prepareIndex(index,type,id).setSource(js).get();
                }else {
                    transportClient.prepareUpdate(index,type,id).setDoc(js).get();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(String index,String type,String id){
        DeleteRequest deleteRequest=new DeleteRequest(index,type,id);
        transportClient.delete(deleteRequest);
    }

    public <T> T findOne(String index,@Nullable String type,String id,Class<T> clazz){
        T r=null;
        try {
            if(type==null){
                type=clazz.getSimpleName();
            }
            GetResponse getResponse=transportClient.prepareGet().setIndex(index)
                    .setType(type).setId(id).get();
            if(getResponse.isExists()){
                r = new ObjectMapper().readValue(getResponse.getSourceAsString(),clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public <T> T findOneByQuery(String index,@Nullable String type,QueryBuilder queryBuilder,Class<T> clazz){
        T r=null;
        try {
            if(type==null)type=clazz.getSimpleName();
            SearchResponse searchResponse= transportClient.prepareSearch(index)
                    .setTypes(type).setQuery(queryBuilder).get();
            if(searchResponse.getHits().totalHits()==0)return null;
            String js= searchResponse.getHits().getAt(0).getSourceAsString();
            r=new ObjectMapper().readValue(js,clazz);
        }catch (Exception e){e.printStackTrace();}
        return r;
    }

    public <T> CloseableIterator<T> streamByQuery(String index, String type, QueryBuilder queryBuilder, Class<T> clazz){

        CloseableIterator<T> iterator=null;
        try {
            if(type==null)type=clazz.getSimpleName();
            SearchResponse searchResponse= transportClient.prepareSearch(index)
                    .setTypes(type).setQuery(queryBuilder).setScroll(new TimeValue(60000))
                    .setSize(1000).get();
            iterator=new CloseableIterator<T>() {
                String scrollId=searchResponse.getScrollId();
                List<SearchHit> hits= Arrays.asList(searchResponse.getHits().getHits());
                Iterator<SearchHit> iterator=hits.iterator();
                @Override
                public boolean hasNext() {
                    if(iterator.hasNext())return true;
                    else{
                        try {
                            SearchResponse sr = transportClient.prepareSearchScroll(scrollId)
                                    .setScroll(new TimeValue(60000)).execute().actionGet();
                            hits=Arrays.asList(sr.getHits().getHits());
                            iterator=hits.iterator();
                            scrollId=sr.getScrollId();
                        }catch (Exception e){
                            //e.printStackTrace();
                        }
                        return iterator.hasNext();
                    }
                }
                @Override
                public T next() {
                    if(hasNext()){
                        T r= null;
                        try {
                            String s=iterator.next().getSourceAsString();
                            r = new ObjectMapper().readValue(s,clazz);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return r;
                    }
                    else{
                        return null;
                    }
                }
                @Override
                public void close() {
                    transportClient.prepareClearScroll().addScrollId(scrollId).get();
                }

            };
        }catch (Exception e){e.printStackTrace();}
        finally {
            iterator.close();
        }
        return iterator;
    }

    public long count(String index,String type){
        long r=0;
        try {
            SearchResponse searchResponse= transportClient.prepareSearch(index).setTypes(type).setQuery(QueryBuilders.matchAllQuery())
                    .setFetchSource(false).get();
            r= searchResponse.getHits().getTotalHits();
        }catch (Exception e){
            e.printStackTrace();
        }
        return r;
    }

    public <T> List<T> findListByQuery(String index, String type, QueryBuilder queryBuilder,int size,Class<T> clazz){
        List<T> r=new ArrayList<T>();
        if(type==null)type=clazz.getSimpleName();
        try {
            SearchResponse searchResponse=transportClient.prepareSearch(index)
                    .setTypes(type).setSize(size)
                    .setQuery(queryBuilder).get();
            for(SearchHit hit:searchResponse.getHits().getHits()){
                try {
                    T temp=new ObjectMapper().readValue(hit.getSourceAsString(),clazz);
                    r.add(temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){e.printStackTrace();}
        return r;
    }


    public String[] findUserViewedIds(int userId,int appId){
        Set<String> r=new HashSet<>();
        String scollId="";
        try {
            QueryBuilder queryBuilder=QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("userId",userId))
                    .must(QueryBuilders.termQuery("appId",appId));
            SearchResponse searchResponse=transportClient.prepareSearch("date:*")
                    .setTypes("action").setSize(1000).setScroll(new TimeValue(60000))
                    .setQuery(queryBuilder).get();
            scollId=searchResponse.getScrollId();
            while (searchResponse.getHits().getHits().length!=0){
                for(SearchHit hit:searchResponse.getHits().getHits()){
                    try {
                        Action action=new ObjectMapper().readValue(hit.getSourceAsString(),Action.class);
                        CommonContentId commonContentId=new CommonContentId(action);
                        r.add(commonContentId.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                searchResponse=transportClient.prepareSearchScroll(scollId)
                        .setScroll(new TimeValue(60000)).execute().actionGet();
                scollId=searchResponse.getScrollId();
            }
            transportClient.prepareClearScroll().addScrollId(scollId).get();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            transportClient.prepareClearScroll().addScrollId(scollId).get();
        }
        return r.toArray(new String[r.size()]);
    }

    public List<String> findRecByCFIndices(){
        GetIndexResponse getIndexResponse= transportClient.admin().indices().prepareGetIndex().get();
        List<String> r=new ArrayList<>();
        r.addAll(Arrays.asList(getIndexResponse.indices()));
        return r.stream().filter(s->s.indexOf(myConfig.recByCFIndex)>-1).collect(Collectors.toList());
    }

//    public void putMapping(String index,XContentBuilder xContentBuilder){
//        if(!transportClient.admin().indices().prepareExists(index).get().isExists()){
//            transportClient.admin().indices().prepareCreate(index).get();
//            transportClient.admin().indices().preparePutMapping(index).setSource(xContentBuilder).get();
//        }
//    }

//    public Set<String> findFieldByQuery(String index, String type, QueryBuilder queryBuilder,String field){
//        SearchResponse searchResponse=transportClient.prepareSearch(index)
//                .setTypes(type).setSize(1000).setScroll(new TimeValue(60000))
//                .setQuery(queryBuilder).get();
//        Set r=new HashSet();
//        while (searchResponse.getHits().getHits().length!=0){
//            for(SearchHit hit:searchResponse.getHits().getHits()){
//                r.add(String.valueOf(hit.getSource().get(field)));
//            }
//            searchResponse=transportClient.prepareSearchScroll(searchResponse.getScrollId())
//                    .setScroll(new TimeValue(60000)).execute().actionGet();
//        }
//        return r;
//    }
//
//    public boolean isIndexExist(String index){
//        IndicesExistsResponse indicesExistsResponse= transportClient.admin().indices().prepareExists(index).get();
//        return indicesExistsResponse.isExists();
//    }
//
//    public void createIndex(String index){
//        transportClient.admin().indices().prepareCreate(index).get();
//    }

}
