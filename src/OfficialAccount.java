import com.oocourse.spec3.main.OfficialAccountInterface;
import com.oocourse.spec3.main.PersonInterface;

import java.util.ArrayList;

public class OfficialAccount implements OfficialAccountInterface {
    private final int ownerId;
    private final int id;
    private final String name;
    private final ArrayList<PersonInterface> followers;
    private final ArrayList<Integer> contributions;
    private final ArrayList<Integer> articleIds;

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        this.followers = new ArrayList<>();
        this.contributions = new ArrayList<>();
        this.articleIds = new ArrayList<>();
    }

    @Override
    public int getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        if (person != null && !containsFollowerById(person.getId())) { 
            followers.add(person);
            contributions.add(0);
        }
    }

    private boolean containsFollowerById(int personId) {
        for (PersonInterface follower : followers) {
            if (follower.getId() == personId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        if (person == null) {
            return false;
        }
        return containsFollowerById(person.getId()); 
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        if (person != null && !containsArticle(id)) { 
            int contributorIndex = 114514;
            for (int i = 0; i < followers.size(); i++) {
                if (followers.get(i).getId() == person.getId()) {
                    contributorIndex = i;
                    break;
                }
            }

            if (contributorIndex != 114514) {
                articleIds.add(id);
                int oldContribution = contributions.get(contributorIndex);
                contributions.set(contributorIndex, oldContribution + 1);
            }
        }
    }

    @Override
    public boolean containsArticle(int id) {
        return articleIds.contains(id); 
    }

    @Override
    public void removeArticle(int id) {
        if (containsArticle(id)) {
            articleIds.remove(Integer.valueOf(id));
        }
    }

    @Override
    public int getBestContributor() {
        if (followers.isEmpty()) {
            return 0; 
        }

        PersonInterface bestContributor = null;
        int maxContributions = -1;

        for (int i = 0; i < followers.size(); i++) {
            int currentContributions = contributions.get(i);
            PersonInterface currentPerson = followers.get(i);

            if (currentContributions > maxContributions) {
                maxContributions = currentContributions;
                bestContributor = currentPerson;
            } else if (currentContributions == maxContributions) {
                if (bestContributor == null || currentPerson.getId() < bestContributor.getId()) {
                    bestContributor = currentPerson;
                }
            }
        }
        return bestContributor != null ? bestContributor.getId() : 0;
    }

    int getId() {
        return id;
    }

    ArrayList<PersonInterface> getFollowers() {
        return followers;
    }

    void updateContribution(int articleContribution) {
        for (int j = 0;j < followers.size();j++) {
            if (followers.get(j).getId() == articleContribution) {
                contributions.set(j, contributions.get(j) - 1);
                break;
            }
        }
    }
}
