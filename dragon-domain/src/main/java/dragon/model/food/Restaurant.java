package dragon.model.food;

/**
 * Created by lin.cheng on 6/16/15.
 */
public class Restaurant {
    String name;
    String link;
    int factor = 5;//1-10
    int score = 10;//0-100
    Long id;

    public Restaurant(String name, String link, int factor, int score, Long id) {
        this.name = name;
        this.link = link;
        this.factor = factor;
        this.score = score;
        this.id = id;
    }

    public Restaurant(String name, String link, int factor, int score) {
        this.name = name;
        this.link = link;
        this.factor = factor;
        this.score = score;
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

    public int getFactor() {
        if(factor < 1){
            factor = 1;
        }
        if(factor > 10){
            factor = 10;
        }
        return factor;
    }

    public void setFactor(int factor) {
        if(factor < 1){
            factor = 1;
        }
        if(factor > 10){
            factor = 10;
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
        return getFactor() * getScore() * 1L;
    }
}
