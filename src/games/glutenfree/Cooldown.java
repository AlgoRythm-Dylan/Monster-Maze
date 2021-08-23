package games.glutenfree;

import org.bukkit.entity.Entity;

import java.util.HashMap;

public class Cooldown {

    HashMap<String, Long> times;
    long rate;
    public Cooldown(long rate){
        times = new HashMap<>();
        this.rate = rate;
    }

    public boolean testEntity(Entity e){
        return test(e.getUniqueId().toString());
    }

    public boolean test(String str){
        if(times.containsKey(str)){
            long lastTime = times.get(str);
            long millis = System.currentTimeMillis();
            if(lastTime + rate <= millis){
                times.put(str, millis);
                return true;
            }
            return false;
        }
        else{
            times.put(str, System.currentTimeMillis());
            return true;
        }
    }

}
