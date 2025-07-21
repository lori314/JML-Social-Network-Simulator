import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.ForwardMessageInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

public class Network implements NetworkInterface {
    private final ArrayList<PersonInterface> persons;
    private final ArrayList<OfficialAccount> accounts;
    private final ArrayList<TagInterface> tags;
    private final ArrayList<Integer> articles;
    private final ArrayList<Integer> articleContributors;
    private int tripleSum = 0;
    private int coupleSumCache = 0;
    private ArrayList<MessageInterface> messages;
    private ArrayList<Integer> emojiIdList;
    private ArrayList<Integer> emojiHeatList;

    public Network() {
        persons = new ArrayList<>();
        accounts = new ArrayList<>();
        articles = new ArrayList<>();
        tags = new ArrayList<>();
        articleContributors = new ArrayList<>();
        messages = new ArrayList<>();
        emojiIdList = new ArrayList<>();
        emojiHeatList = new ArrayList<>();
    }

    public boolean containsPerson(int id) {
        for (PersonInterface person : persons) {
            if (person.getId() == id) { return true; }
        }
        return false;
    }

    public PersonInterface getPerson(int id) {
        for (PersonInterface person : persons) {
            if (person.getId() == id) { return person; }
        }
        return null;
    }

    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        if (containsPerson(person.getId())) {
            throw new EqualPersonIdException(person.getId());
        }
        persons.add(person);
    }

    public void addRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualRelationException {
        if (!containsPerson(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!containsPerson(id2)) { throw new PersonIdNotFoundException(id2); }
        if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new EqualRelationException(id1, id2);
        }
        HashSet<Integer> relevantPeople = new HashSet<>();
        relevantPeople.add(id1);
        relevantPeople.add(id2);
        int id1OldBest = myQueryBestAcquaintance(id1);
        if (id1OldBest != -626520) { relevantPeople.add(id1OldBest); }
        int id2OldBest = myQueryBestAcquaintance(id2);
        if (id2OldBest != -626520) { relevantPeople.add(id2OldBest); }
        for (int personInvolvedId : relevantPeople) {
            int currentBestAcq = myQueryBestAcquaintance(personInvolvedId);
            if (currentBestAcq != id1 && currentBestAcq != id2) {
                if (personInvolvedId != id1 && personInvolvedId != id2) {
                    continue;
                }
            }
            if (personInvolvedId < currentBestAcq) {
                int partnerOfCurrentBest = myQueryBestAcquaintance(currentBestAcq);
                if (partnerOfCurrentBest == personInvolvedId) {
                    coupleSumCache--;
                }
            }
        }
        PersonInterface person1 = getPerson(id1);
        PersonInterface person2 = getPerson(id2);
        ((Person) person1).setValue(person2, value);
        ((Person) person2).setValue(person1, value);
        updateTagValueSum(person1,person2,value);
        for (PersonInterface person : ((Person) person1).getAcquaintance()) {
            if (((Person) person2).getAcquaintance().contains(person)) {
                tripleSum += 1;
            }
        }
        decref(id1, id2, relevantPeople);
    }

    private void updateTagValueSum(PersonInterface person1, PersonInterface person2, int value) {
        for (TagInterface tag : tags) {
            if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                ((Tag)tag).updateTagValueSum(value * 2);
            }
        }
    }

    private void decref(int id1, int id2, HashSet<Integer> relevantPeople) {
        int id1NewBest = myQueryBestAcquaintance(id1);
        if (id1NewBest != -626520) { relevantPeople.add(id1NewBest); }
        int id2NewBest = myQueryBestAcquaintance(id2);
        if (id2NewBest != -626520) { relevantPeople.add(id2NewBest); }
        for (int personInvolvedId : relevantPeople) {
            int currentBestAcq = myQueryBestAcquaintance(personInvolvedId);
            if (currentBestAcq != id1 && currentBestAcq != id2) {
                if (personInvolvedId != id1 && personInvolvedId != id2) {
                    continue;
                }
            }
            if (personInvolvedId < currentBestAcq) {
                int partnerOfCurrentBest = myQueryBestAcquaintance(currentBestAcq);
                if (partnerOfCurrentBest != -626520 && partnerOfCurrentBest == personInvolvedId) {
                    coupleSumCache++;
                }
            }
        }
    }

    public void modifyRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!containsPerson(id1)) { throw new PersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new PersonIdNotFoundException(id2); }
        if (id1 == id2) { throw new EqualPersonIdException(id1); }
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
        HashSet<Integer> relevantPeople = new HashSet<>();
        relevantPeople.add(id1);
        relevantPeople.add(id2);
        int id1OldBest = myQueryBestAcquaintance(id1);
        if (id1OldBest != -626520) { relevantPeople.add(id1OldBest); }
        int id2OldBest = myQueryBestAcquaintance(id2);
        if (id2OldBest != -626520) { relevantPeople.add(id2OldBest); }
        for (int personInvolvedId : relevantPeople) {
            int currentBestAcq = myQueryBestAcquaintance(personInvolvedId);
            if (personInvolvedId < currentBestAcq) {
                int partnerOfCurrentBest = myQueryBestAcquaintance(currentBestAcq);
                if (currentBestAcq != id1 && currentBestAcq != id2) {
                    if (personInvolvedId != id1 && personInvolvedId != id2) {
                        continue;
                    }
                }
                if (partnerOfCurrentBest == personInvolvedId) {
                    coupleSumCache--;
                }
            }
        }
        PersonInterface person1 = getPerson(id1);
        PersonInterface person2 = getPerson(id2);
        if (queryValue(id1,id2) + value > 0) {
            updateTagValueSum(person1,person2,value);
        }
        if (queryValue(id1,id2) + value <= 0) {
            updateTagValueSum(person1,person2,-queryValue(id1,id2));
            for (PersonInterface person : ((Person) person1).getAcquaintance()) {
                if (((Person) person2).getAcquaintance().contains(person)) {
                    tripleSum -= 1;
                }
            }
        }
        ((Person) person1).modifyAcquaintanceInternal(person2, value);
        ((Person) person2).modifyAcquaintanceInternal(person1, value);
        decref(id1, id2, relevantPeople);
    }

    public int queryValue(int id1, int id2)
        throws PersonIdNotFoundException, RelationNotFoundException {
        if (!containsPerson(id1)) { throw new PersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new PersonIdNotFoundException(id2); }
        if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
        return getPerson(id1).queryValue(getPerson(id2));
    }

    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!containsPerson(id1)) { throw new PersonIdNotFoundException(id1); }
        else if (!containsPerson(id2)) { throw new PersonIdNotFoundException(id2); }
        if (getPerson(id1).isLinked(getPerson(id2))) { return true; }
        PersonInterface startPerson = getPerson(id1);
        PersonInterface targetPerson = getPerson(id2);
        LinkedList<PersonInterface> queue = new LinkedList<>();
        HashSet<Integer> visitedIds = new HashSet<>();
        queue.offer(startPerson);
        visitedIds.add(startPerson.getId());
        while (!queue.isEmpty()) {
            PersonInterface currentPerson = queue.poll();
            if (currentPerson != null) {
                ArrayList<PersonInterface> acquaintances
                    = ((Person) currentPerson).getAcquaintance();
                for (PersonInterface neighbor : acquaintances) {
                    if (neighbor.equals(targetPerson)) { return true; }
                    if (!visitedIds.contains(neighbor.getId())) {
                        visitedIds.add(neighbor.getId());
                        queue.offer(neighbor);
                    }
                }
            }
        }
        return false;
    }

    public int queryTripleSum() {
        return tripleSum;
    }

    public void addTag(int personId, TagInterface tag)
        throws PersonIdNotFoundException, EqualTagIdException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        PersonInterface person = getPerson(personId);
        if (person.containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId());
        }
        person.addTag(tag);
        tags.add(tag);
    }

    public void addPersonToTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) { throw new PersonIdNotFoundException(personId1); }
        if (!containsPerson(personId2)) { throw new PersonIdNotFoundException(personId2); }
        if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        }
        PersonInterface person1 = getPerson(personId1);
        PersonInterface person2 = getPerson(personId2);
        if (!person2.isLinked(person1)) {
            throw new RelationNotFoundException(personId1, personId2);
        }
        if (!person2.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        TagInterface tag = person2.getTag(tagId);
        if (tag.hasPerson(person1)) {
            throw new EqualPersonIdException(personId1);
        }
        if (tag.getSize() <= 999) {
            tag.addPerson(person1);
        }
    }

    public int queryTagValueSum(int personId, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        PersonInterface person = getPerson(personId);
        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        TagInterface tag = person.getTag(tagId);
        return tag.getValueSum();
    }

    public int queryTagAgeVar(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        PersonInterface person = getPerson(personId);
        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        TagInterface tag = person.getTag(tagId);
        return tag.getAgeVar();
    }

    public void delPersonFromTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        }
        PersonInterface person1 = getPerson(personId1);
        PersonInterface person2 = getPerson(personId2);
        if (!person2.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        TagInterface tag = person2.getTag(tagId);
        if (!tag.hasPerson(person1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        tag.delPerson(person1);
    }

    public void delTag(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        PersonInterface person = getPerson(personId);
        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        person.delTag(tagId);
    }

    public boolean containsMessage(int id) {
        for (MessageInterface messageInterface : messages) {
            if (messageInterface.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public void addMessage(MessageInterface message) throws EqualMessageIdException,
        EmojiIdNotFoundException, EqualPersonIdException, ArticleIdNotFoundException {
        if (containsMessage(message.getId())) {
            throw new EqualMessageIdException(message.getId());
        }
        else {
            if (message instanceof EmojiMessageInterface &&
                !containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) {
                throw new EmojiIdNotFoundException(((EmojiMessageInterface) message).getEmojiId());
            }
            else if (message instanceof ForwardMessageInterface) {
                if (!containsArticle(((ForwardMessageInterface) message).getArticleId())) {
                    throw new ArticleIdNotFoundException(
                        ((ForwardMessageInterface) message).getArticleId());
                }
                else if (!message.getPerson1().getReceivedArticles().contains(
                    ((ForwardMessageInterface) message).getArticleId())) {
                    throw new ArticleIdNotFoundException(
                        ((ForwardMessageInterface) message).getArticleId());
                }
            }
        }
        if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        }
        messages.add(message);
    }

    public MessageInterface getMessage(int id) {
        MessageInterface message = null;
        for (MessageInterface messageInterface : messages) {
            if (messageInterface.getId() == id) {
                message = messageInterface;
                break;
            }
        }
        return message;
    }

    public void sendMessage(int id) throws RelationNotFoundException,
        MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        }
        MessageInterface message = getMessage(id);
        PersonInterface person1 = message.getPerson1();
        if (message.getType() == 0 && !person1.isLinked(message.getPerson2())) {
            throw new RelationNotFoundException(person1.getId(), message.getPerson2().getId());
        }
        if (message.getType() == 1 && !person1.containsTag(message.getTag().getId())) {
            throw new TagIdNotFoundException(message.getTag().getId());
        }
        if (message.getType() == 0) {
            PersonInterface person2 = message.getPerson2();
            messages.remove(message);
            person1.addSocialValue(message.getSocialValue());
            person2.addSocialValue(message.getSocialValue());
            updateEmojiHeatList(message);
            if (message instanceof RedEnvelopeMessageInterface) {
                person1.addMoney(-((RedEnvelopeMessageInterface) message).getMoney());
                person2.addMoney(((RedEnvelopeMessageInterface) message).getMoney());
            }
            if (message instanceof ForwardMessageInterface) {
                ((Person)person2).updateReceivedArticles(
                    ((ForwardMessageInterface) message).getArticleId());
            }
            ((Person)person2).updateMessages(message);
        }
        if (message.getType() == 1) {
            TagInterface tag = message.getTag();
            messages.remove(message);
            person1.addSocialValue(message.getSocialValue());
            for (PersonInterface p : ((Tag) tag).getPersons()) {
                p.addSocialValue(message.getSocialValue());
                ((Person) p).updateMessages(message);
            }
            updateEmojiHeatList(message);
            if (message instanceof RedEnvelopeMessageInterface) {
                if (tag.getSize() == 0) { return; }
                int money = ((RedEnvelopeMessageInterface) message).getMoney();
                int send = money / tag.getSize();
                person1.addMoney(-(send * tag.getSize()));
                for (PersonInterface p : ((Tag) tag).getPersons()) {
                    p.addMoney(send);
                }
            }
            if (message instanceof ForwardMessageInterface) {
                for (PersonInterface p : ((Tag) tag).getPersons()) {
                    ((Person)p).updateReceivedArticles(
                        ((ForwardMessageInterface) message).getArticleId());
                }
            }
        }
    }

    private void updateEmojiHeatList(MessageInterface message) {
        if (message instanceof EmojiMessageInterface) {
            for (int i = 0;i < emojiIdList.size(); i++) {
                if (emojiIdList.get(i) == ((EmojiMessageInterface) message).getEmojiId()) {
                    emojiHeatList.set(i, emojiHeatList.get(i) + 1);
                    break;
                }
            }
        }
    }

    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        return getPerson(id).getSocialValue();
    }

    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        return getPerson(id).getReceivedMessages();
    }

    public boolean containsEmojiId(int id) { return emojiIdList.contains(id); }

    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) { throw new EqualEmojiIdException(id); }
        emojiIdList.add(id);
        emojiHeatList.add(0);
    }

    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        return getPerson(id).getMoney();
    }

    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        int index = -114514;
        for (int i = 0;i < emojiIdList.size();i++) {
            if (emojiIdList.get(i).equals(id)) {
                index = i;
                break;
            }
        }
        if (index != -114514) { return emojiHeatList.get(index); }
        else { throw new EmojiIdNotFoundException(id); }
    }

    public int deleteColdEmoji(int limit) {
        ArrayList<Integer> newEmojiIdList = new ArrayList<>();
        ArrayList<Integer> newEmojiHeatList = new ArrayList<>();
        int oldEmojiSize = emojiIdList.size();
        for (int i = 0; i < oldEmojiSize; i++) {
            int id = this.emojiIdList.get(i);
            int heat = this.emojiHeatList.get(i);
            if (heat >= limit) {
                newEmojiIdList.add(id);
                newEmojiHeatList.add(heat);
            }
        }
        this.emojiIdList = newEmojiIdList;
        this.emojiHeatList = newEmojiHeatList;
        HashSet<Integer> keptEmojiIds = new HashSet<>(newEmojiIdList);
        ArrayList<MessageInterface> newMessagesList = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            MessageInterface message = messages.get(i);
            if (!(message instanceof EmojiMessageInterface)) {
                newMessagesList.add(message);
            } else {
                EmojiMessageInterface emojiMessage = (EmojiMessageInterface) message;
                if (keptEmojiIds.contains(emojiMessage.getEmojiId())) {
                    newMessagesList.add(message);
                }
            }
        }
        messages = newMessagesList;
        return this.emojiIdList.size();
    }

    public int queryBestAcquaintance(int id)
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        Person person = (Person) getPerson(id);
        if (person.getAcquaintance().isEmpty()) { throw new AcquaintanceNotFoundException(id); }
        return person.getBestFriend().getId();
    }

    public int queryCoupleSum() { return coupleSumCache; }

    private int myQueryBestAcquaintance(int id) {
        Person person = (Person) getPerson(id);
        if (person == null || person.getAcquaintance().isEmpty()) { return -626520; }
        return person.getBestFriend().getId();
    }

    public int queryShortestPath(int id1, int id2) throws
        PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) { throw new PersonIdNotFoundException(id1); }
        if (!containsPerson(id2)) { throw new PersonIdNotFoundException(id2); }
        if (id1 == id2) { return 0; }
        LinkedList<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> distance = new HashMap<>();
        HashSet<Integer> visitedIds = new HashSet<>();
        queue.offer(id1);
        distance.put(id1, 0);
        visitedIds.add(id1);
        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            if (currentId == id2) { return distance.get(currentId); }
            PersonInterface currentPerson = getPerson(currentId);
            ArrayList<PersonInterface> acquaintances = ((Person) currentPerson).getAcquaintance();
            for (PersonInterface neighbor : acquaintances) {
                int neighborId = neighbor.getId();
                if (!visitedIds.contains(neighborId)) {
                    visitedIds.add(neighborId);
                    distance.put(neighborId, distance.get(currentId) + 1);
                    queue.offer(neighborId);
                }
            }
        }
        throw new PathNotFoundException(id1, id2);
    }

    public boolean containsAccount(int id) {
        for (OfficialAccount account : accounts) {
            if (account.getId() == id) { return true; }
        }
        return false;
    }

    public void createOfficialAccount(int personId, int accountId, String name) throws
        PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (containsAccount(accountId)) { throw new EqualOfficialAccountIdException(accountId); }
        OfficialAccount account = new OfficialAccount(personId, accountId, name);
        account.addFollower(getPerson(personId));
        accounts.add(account);
    }

    public void deleteOfficialAccount(int personId, int accountId) throws PersonIdNotFoundException
        , OfficialAccountIdNotFoundException, DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount temp = getAccount(accountId);
        if (temp.getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
        accounts.remove(temp);
    }

    public boolean containsArticle(int id) {
        for (Integer articleId : articles) {
            if (articleId == id) { return true; }
        }
        return false;
    }

    public void contributeArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount officialAccount = getAccount(accountId);
        if (containsArticle(articleId)) { throw new EqualArticleIdException(articleId); }
        PersonInterface contributorPerson = getPerson(personId);
        if (!officialAccount.containsFollower(contributorPerson)) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }
        articles.add(articleId);
        articleContributors.add(personId);
        officialAccount.addArticle(contributorPerson,articleId);
        for (PersonInterface follower : officialAccount.getFollowers()) {
            ((Person) follower).updateReceivedArticles(articleId);
        }
    }

    private OfficialAccount getAccount(int accountId) {
        OfficialAccount temp = null;
        for (OfficialAccount account : accounts) {
            if (account.getId() == accountId) {
                temp = account;
                break;
            }
        }
        return temp;
    }

    public void deleteArticle(int personId, int accountId, int articleId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount officialAccount = getAccount(accountId);
        if (!officialAccount.containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
        if (officialAccount.getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId,articleId);
        }
        officialAccount.removeArticle(articleId);
        int index = 114514;
        for (int i = 0;i < articles.size();i++) {
            if (articles.get(i) == articleId) { index = i; }
        }
        officialAccount.updateContribution(articleContributors.get(index));
        for (PersonInterface follower : officialAccount.getFollowers()) {
            ((Person) follower).removeArticle(articleId);
        }
    }

    public void followOfficialAccount(int personId, int accountId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) { throw new PersonIdNotFoundException(personId); }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        PersonInterface person = getPerson(personId);
        OfficialAccount officialAccount = getAccount(accountId);
        if (officialAccount.containsFollower(person)) {
            throw new EqualPersonIdException(personId);
        }
        officialAccount.addFollower(person);
    }

    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) { throw new OfficialAccountIdNotFoundException(id); }
        OfficialAccount temp = getAccount(id);
        return temp.getBestContributor();
    }

    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) { throw new PersonIdNotFoundException(id); }
        PersonInterface person = getPerson(id);
        return person.queryReceivedArticles();
    }

    public PersonInterface[] getPersons() {
        return persons.toArray(new PersonInterface[0]);
    }

    public MessageInterface[] getMessages() {
        return messages.toArray(new MessageInterface[0]);
    }

    public int[] getEmojiIdList() {
        int[] result = new int[emojiIdList.size()];
        for (int i = 0; i < emojiIdList.size(); i++) { result[i] = emojiIdList.get(i); }
        return result;
    }

    public int[] getEmojiHeatList() {
        int[] result = new int[emojiHeatList.size()];
        for (int i = 0; i < emojiHeatList.size(); i++) { result[i] = emojiHeatList.get(i); }
        return result;
    }
}
