package dragon.model.food;

import org.apache.commons.lang.StringUtils;

/**
 * Created by lin.cheng on 6/16/15.
 */
public class Restaurant {
    String name;
    String link;
    int factor = 5;//1-20
    int score = 20;//0-100
    Long id;
    String alias;
    String category;

    public Restaurant(String name, String link, int factor, int score, Long id, String alias, String category) {
        this.name = name;
        this.link = link;
        this.factor = factor;
        this.score = score;
        this.alias = alias;
        this.category = category;
        this.id = id;
    }

    public Restaurant(String name, String link, int factor, int score, String alias, String category) {
        this.name = name;
        this.link = link;
        this.factor = factor;
        this.score = score;
        this.alias = alias;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        if(StringUtils.isNotBlank(alias)) {
            return alias;
        }
        return name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getFactor() {
        if(factor < 1){
            factor = 1;
        }
        if(factor > 20){
            factor = 20;
        }
        return factor;
    }

    public void setFactor(int factor) {
        if(factor < 1){
            factor = 1;
        }
        if(factor > 20){
            factor = 20;
        }
        this.factor = factor;
    }

    public int getScore() {
        if(score < 1){
            score = 1;
        }
        if(score > 100){
            score = 100;
        }
        return score;
    }

    public void setScore(int score) {
        if(score < 1){
            score = 1;
        }
        if(score > 100){
            score = 100;
        }
        this.score = score;
    }

    public Long getWeight(){
        return getFactor() * 1L;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name: '" + name + '\'' +
                ", link: '" + link + '\'' +
                ", factor: " + factor +
                ", score: " + score +
                ", alias: " + alias +
                ", category: " + category +
                '}';
    }
}
