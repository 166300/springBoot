package com.atbm.es.esdemo;

import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class EsDemoApplicationTests {

    @Autowired
    JestClient jestClient;



    @Test
    void contextLoads() {
        System.out.println(jestClient);
    }
    @Test
    void escrud() throws IOException {
        User user = new User();
        user.setUserName("小明");user.setEmail("123@qq.com");
        //添加数据
        //Index index = new crud.Builder(数据),其他信息.build();
        //DocumentResult execute = jestClient.execute(index);
        Index index = new Index.Builder(user)
                .index("user")
                .type("info")
                .build();
        DocumentResult execute = jestClient.execute(index);
        System.out.println(execute.getId()+"==>"+execute.isSucceeded());
    }

    @Test
    void query() throws IOException {
        //{"query":"{"match_all":"{}"}"}
        String queryJson ="";
        Search search = new Search.Builder(queryJson).addIndex("user").build();
        SearchResult execute = jestClient.execute(search);
        System.out.println("总记录数"+execute.getTotal()+"==>最大得分："+execute.getMaxScore());
        List<SearchResult.Hit<User, Void>> hits = execute.getHits(User.class);
        hits.forEach((hit)->{
            User user = hit.source;
            System.out.println("查到的数据："+user.getUserName());
        });
    }

class User{
    private  String userName;
    private  String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

}
