package dragon.model.food;

import org.apache.commons.lang.StringUtils;

/**
 * Created by lin.cheng on 6/16/15.
 */
public class Restaurant {
    String name;
    String link;
    Long factor = 5L;//1-30
    Long id;
    String alias;
    String category;

    public Restaurant(String name, String link, Long factor, Long id, String alias, String category) {
        this.name = name;
        this.link = link;
        this.factor = factor;
        this.alias = alias;
        this.category = category;
        this.id = id;
    }

    public Restaurant(String name, String link, Long factor, String alias, String category) {
        this.name = name;
        this.link = link;
        this.factor = factor;
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

    public Long getFactor() {
        if(factor < 1){
            factor = 1L;
        }
        if(factor > 30){
            factor = 30L;
        }
        return factor;
    }

    public void setFactor(Long factor) {
        if(factor < 1){
            factor = 1L;
        }
        if(factor > 30){
            factor = 30L;
        }
        this.factor = factor;
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
                ", alias: " + alias +
                ", category: " + category +
                '}';
    }
}
