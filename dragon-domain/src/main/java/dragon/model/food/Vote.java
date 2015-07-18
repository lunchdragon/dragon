package dragon.model.food;

/**
 * Created by lin.cheng on 6/19/15.
 */
public class Vote {
    String email;
    Long recId;
    Result result;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getRecId() {
        return recId;
    }

    public void setRecId(Long recId) {
        this.recId = recId;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "email='" + email + '\'' +
                ", recId=" + recId +
                ", result=" + result +
                '}';
    }

    public static enum Result {
        killme(-2),
        dislike(-1),
        like(2);

        int score;

        public int getScore() {
            return score;
        }

        Result(int score) {
            this.score = score;
        }
    }
}
