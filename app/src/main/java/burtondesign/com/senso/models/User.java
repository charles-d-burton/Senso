package burtondesign.com.senso.models;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by charles on 1/8/17.
 */

public class User extends RealmObject {


    @Required
    public String apiKey;
    public long userId;
    public String email;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
