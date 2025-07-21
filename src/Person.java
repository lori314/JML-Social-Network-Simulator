import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

public class Person implements PersonInterface {
    private String name;
    private int age;
    private final int id;
    private PersonInterface bestFriend;
    private ArrayList<PersonInterface> acquaintance;
    private ArrayList<TagInterface> tags;
    private Map<Integer,Integer> values;
    private ArrayList<Integer> receivedArticles;
    private HashSet<Integer> logicallyRemovedArticleIds;
    private int money;
    private int socialValue;
    private ArrayList<MessageInterface> messages;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.bestFriend = null;
        acquaintance = new ArrayList<>();
        tags = new ArrayList<>();
        values = new HashMap<>();
        receivedArticles = new ArrayList<>();
        logicallyRemovedArticleIds = new HashSet<>();
        this.money = 0;
        this.socialValue = 0;
        messages = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean containsTag(int id) {
        for (TagInterface tag : tags) {
            if (tag.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public TagInterface getTag(int id) {
        for (TagInterface tag : tags) {
            if (tag.getId() == id) {
                return tag;
            }
        }
        return null;
    }

    public void addTag(TagInterface tag) {
        tags.add(tag);
    }

    public void delTag(int id) {
        tags.remove(getTag(id));
    }

    public boolean equals(Object obj) {
        if (obj instanceof PersonInterface) {
            PersonInterface person = (PersonInterface) obj;
            return person.getId() == id;
        }
        return false;
    }

    public boolean isLinked(PersonInterface person) {
        if (person.getId() == id) {
            return true;
        }
        return acquaintance.contains(person);
    }

    public int queryValue(PersonInterface person) {
        if (person.getId() == id) {
            return 0;
        }
        return values.get(person.getId());
    }

    public List<Integer> getReceivedArticles() {
        return receivedArticles;
    }

    public List<Integer> queryReceivedArticles() {
        performCleanup();
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0;i < receivedArticles.size();i++) {
            if (i <= 4) {
                result.add(receivedArticles.get(i));
            }
            else {
                break;
            }
        }
        return result;
    }

    public void addSocialValue(int num) {
        socialValue += num;
    }

    public int getSocialValue() {
        return socialValue;
    }

    public List<MessageInterface> getMessages() {
        return messages;
    }

    public List<MessageInterface> getReceivedMessages() {
        performCleanup();
        ArrayList<MessageInterface> result = new ArrayList<>();
        for (int i = 0;i < messages.size();i++) {
            if (i <= 4) {
                result.add(messages.get(i));
            }
            else {
                break;
            }
        }
        return result;
    }

    public void addMoney(int num) {
        money += num;
    }

    public int getMoney() {
        return money;
    }

    void setValue(PersonInterface person, int value) {
        acquaintance.add(person);
        values.put(person.getId(), value);
        if (bestFriend == null) {
            bestFriend = person;
        }
        else {
            if (values.get(bestFriend.getId()) < value) {
                bestFriend = person;
            }
            else if (values.get(bestFriend.getId()) == value) {
                if (person.getId() < bestFriend.getId()) {
                    bestFriend = person;
                }
            }
        }
    }

    void modifyAcquaintanceInternal(PersonInterface person, int value) {
        int currentValue = values.get(person.getId());
        int newValue = currentValue + value;
        if (newValue > 0) {
            values.replace(person.getId(), newValue);
            if (person.equals(bestFriend) && value < 0) {
                bestFriend = findBestFriend();
                return;
            }
            if (values.get(bestFriend.getId()) < newValue) {
                bestFriend = person;
            }
            else if (values.get(bestFriend.getId()) == newValue) {
                if (person.getId() < bestFriend.getId()) {
                    bestFriend = person;
                }
            }
        } else {
            for (TagInterface tag : tags) {
                if (tag.hasPerson(person)) {
                    tag.delPerson(person);
                }
            }
            acquaintance.remove(person);
            values.remove(person.getId());
            bestFriend = findBestFriend();
        }
    }

    private PersonInterface findBestFriend() {
        if (acquaintance.isEmpty()) {
            return null;
        }
        else {
            int index = -1;
            int maxValue = 0;
            int minId = 0;
            for (int i = 0;i < acquaintance.size();i++) {
                PersonInterface person = acquaintance.get(i);
                if (values.get(person.getId()) > maxValue) {
                    maxValue = values.get(person.getId());
                    index = i;
                    minId = person.getId();
                }
                else if (values.get(person.getId()) == maxValue) {
                    if (person.getId() < minId) {
                        minId = person.getId();
                        index = i;
                    }
                }
            }
            return acquaintance.get(index);
        }
    }

    void updateReceivedArticles(int articleId) {
        receivedArticles.add(0,articleId);
    }

    void updateMessages(MessageInterface message) { messages.add(0,message); }

    void removeArticle(int articleId) {
        logicallyRemovedArticleIds.add(articleId);
    }

    private void performCleanup() {
        if (logicallyRemovedArticleIds.isEmpty()) {
            return;
        }
        ArrayList<Integer> tempList = new ArrayList<>();
        for (Integer articleId : receivedArticles) {
            if (articleId != null && !logicallyRemovedArticleIds.contains(articleId)) {
                tempList.add(articleId);
            }
        }
        receivedArticles = tempList;
        logicallyRemovedArticleIds.clear();
    }

    ArrayList<PersonInterface> getAcquaintance() {
        return acquaintance;
    }

    PersonInterface getBestFriend() {
        return bestFriend;
    }

    public boolean strictEquals(PersonInterface person) {
        if (!(person.getId() == id)) {
            return false;
        }
        if (!(person.getName().equals(name))) {
            return false;
        }
        return person.getAge() == age;
    }
}
