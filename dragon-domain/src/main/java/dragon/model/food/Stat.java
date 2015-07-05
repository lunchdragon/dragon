package dragon.model.food;

/**
 * Created by lin.cheng on 6/22/15.
 */
public class Stat {
    private String name;
    private int liked = 0;
    private int disliked = 0;
    private int vetoed = 0;
    private int factor;
    private int score;
    private int selected = 0;

    public Stat(String name, int factor, int score) {
        this.name = name;
        this.factor = factor;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    public int getDisliked() {
        return disliked;
    }

    public void setDisliked(int disliked) {
        this.disliked = disliked;
    }

    public int getVetoed() {
        return vetoed;
    }

    public void setVetoed(int vetoed) {
        this.vetoed = vetoed;
    }

    public int getFactor() {
        return factor;
    }

    public void setFactor(int factor) {
        this.factor = factor;
    }

    public int getRawScore() {
        return score;
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
        this.score = score;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "name: " + name +
                ", factor: " + factor +
                ", score: " + score +
                ", selected: " + selected +
                ", liked: " + liked +
                ", disliked: " + disliked +
                ", vetoed: " + vetoed;
    }

    public String toPrintString() {
        return String.format("%-35s%-10s%-10s%-10s%-10s%-10s%-10s", name, getFactor(), getScore(), getSelected(), getLiked(), getDisliked(), getVetoed());
    }

}
