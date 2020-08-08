package org.springframework.netty.http;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public class BaseInfo {

    private String id;
    private String name;

    private Integer age;
    private String nickname;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
