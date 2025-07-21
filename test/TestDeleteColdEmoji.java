import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestDeleteColdEmoji {
    private Network network;
    private Network network2;
    private Set<Integer> allPersonIds;
    private Map<Integer, Set<Integer>> personOwnedTagIds;
    private Set<Integer> allStoredEmojiIds;
    private Set<Integer> allGlobalArticleIds;
    private Set<Integer> allAddedMessageIds;

    private PersonInterface createPerson(int id, String name, int age) {
        return new Person(id, name, age);
    }

    private TagInterface createTag(int id) {
        return new Tag(id);
    }

    private MessageInterface createMessage(int id, int socialValue, int type, PersonInterface person1, PersonInterface person2, TagInterface tag) {
        if (type == 0) {
            return new Message(id, socialValue, person1, person2);
        } else {
            return new Message(id, socialValue, person1, tag);
        }
    }

    private EmojiMessageInterface createEmojiMessage(int id, int emojiId, int type, PersonInterface person1, PersonInterface person2, TagInterface tag) {
        if (type == 0) {
            return new EmojiMessage(id, emojiId, person1, person2);
        } else {
            return new EmojiMessage(id, emojiId, person1, tag);
        }
    }

    private RedEnvelopeMessageInterface createRedEnvelopeMessage(int id, int money, int type, PersonInterface person1, PersonInterface person2, TagInterface tag) {
        if (type == 0) {
            return new RedEnvelopeMessage(id, money, person1, person2);
        } else {
            return new RedEnvelopeMessage(id, money, person1, tag);
        }
    }

    private ForwardMessageInterface createForwardMessage(int id, int articleId, int type, PersonInterface person1, PersonInterface person2, TagInterface tag) {
        if (type == 0) {
            return new ForwardMessage(id, articleId, person1, person2);
        } else {
            return new ForwardMessage(id, articleId, person1, tag);
        }
    }

    @Before
    public void setUp() throws Exception {
        network = new Network();
        network2 = new Network();
        allPersonIds = new HashSet<>();
        personOwnedTagIds = new HashMap<>();
        allStoredEmojiIds = new HashSet<>();
        allGlobalArticleIds = new HashSet<>();
        allAddedMessageIds = new HashSet<>();
    }

    private void addPerson(int id, String name, int age) throws EqualPersonIdException {
        PersonInterface person = createPerson(id, name, age);
        PersonInterface person2 = createPerson(id, name, age);
        network.addPerson(person);
        network2.addPerson(person2);
        allPersonIds.add(id);
        personOwnedTagIds.putIfAbsent(id, new HashSet<>());
    }

    private void addRelation(int id1, int id2, int value) throws PersonIdNotFoundException, EqualRelationException {
        network.addRelation(id1, id2, value);
        network2.addRelation(id1, id2, value);
    }

    private void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        network.modifyRelation(id1, id2, value);
        network2.modifyRelation(id1, id2, value);
    }

    private void addTag(int personId, int tagId) throws PersonIdNotFoundException, EqualTagIdException {
        TagInterface tag = createTag(tagId);
        TagInterface tag2 = createTag(tagId);
        network.addTag(personId, tag);
        network2.addTag(personId, tag2);
        personOwnedTagIds.putIfAbsent(personId, new HashSet<>());
        personOwnedTagIds.get(personId).add(tagId);
    }

    private void addPersonToTag(int personId1, int personId2, int tagId) throws PersonIdNotFoundException, RelationNotFoundException, TagIdNotFoundException, EqualPersonIdException {
        network.addPersonToTag(personId1, personId2, tagId);
        network2.addPersonToTag(personId1, personId2, tagId);
    }

    private void delTag(int personId, int tagId) throws PersonIdNotFoundException, TagIdNotFoundException {
        network.delTag(personId, tagId);
        network2.delTag(personId, tagId);
        if (personOwnedTagIds.containsKey(personId)) {
            personOwnedTagIds.get(personId).remove(tagId);
        }
    }

    private void createOfficialAccount(int personId, int accountId, String name) throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        network.createOfficialAccount(personId, accountId, name);
        network2.createOfficialAccount(personId, accountId, name);
    }

    private void contributeArticle(int personId, int accountId, int articleId) throws PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualArticleIdException, ContributePermissionDeniedException {
        network.contributeArticle(personId, accountId, articleId);
        network2.contributeArticle(personId, accountId, articleId);
        allGlobalArticleIds.add(articleId);
    }

    private void followOfficialAccount(int personId, int accountId) throws PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualPersonIdException {
        network.followOfficialAccount(personId, accountId);
        network2.followOfficialAccount(personId, accountId);
    }

    private void storeEmojiId(int id) throws EqualEmojiIdException {
        network.storeEmojiId(id);
        network2.storeEmojiId(id);
        allStoredEmojiIds.add(id);
    }

    private void addMessageHelper(int id, int content, int type, int id1, int id2, String msgType) throws Exception {
        PersonInterface p1 = network.getPerson(id1);
        PersonInterface p1_2 = network2.getPerson(id1);

        if (p1 == null) throw new PersonIdNotFoundException(id1);

        PersonInterface p2 = null;
        PersonInterface p2_2 = null;
        TagInterface tag = null;
        TagInterface tag2 = null;

        if (type == 0) {
            p2 = network.getPerson(id2);
            p2_2 = network2.getPerson(id2);
            if (p2 == null) throw new PersonIdNotFoundException(id2);
        } else {
            tag = p1.getTag(id2);
            tag2 = p1_2.getTag(id2);
            if (tag == null) throw new TagIdNotFoundException(id2);
        }

        if (msgType.equals("FM")) {
            if (!p1.queryReceivedArticles().contains(content)) {
                throw new ArticleIdNotFoundException(content);
            }
        }

        MessageInterface message = null;
        MessageInterface message2 = null;

        if (msgType.equals("OM")) {
            message = createMessage(id, content, type, p1, p2, tag);
            message2 = createMessage(id, content, type, p1_2, p2_2, tag2);
        } else if (msgType.equals("RE")) {
            message = createRedEnvelopeMessage(id, content, type, p1, p2, tag);
            message2 = createRedEnvelopeMessage(id, content, type, p1_2, p2_2, tag2);
        } else if (msgType.equals("FM")) {
            message = createForwardMessage(id, content, type, p1, p2, tag);
            message2 = createForwardMessage(id, content, type, p1_2, p2_2, tag2);
        } else if (msgType.equals("EM")) {
            message = createEmojiMessage(id, content, type, p1, p2, tag);
            message2 = createEmojiMessage(id, content, type, p1_2, p2_2, tag2);
        } else {
            throw new IllegalArgumentException("Unknown message type: " + msgType);
        }

        network.addMessage(message);
        network2.addMessage(message2);
        allAddedMessageIds.add(id);
    }

    private void addMessage(int id, int socialValue, int type, int id1, int id2) {
        try { addMessageHelper(id, socialValue, type, id1, id2, "OM"); }
        catch (Exception e) { }
    }

    private void addRedEnvelopeMessage(int id, int money, int type, int id1, int id2) {
        try { addMessageHelper(id, money, type, id1, id2, "RE"); }
        catch (Exception e) { }
    }

    private void addForwardMessage(int id, int articleId, int type, int id1, int id2) {
        try { addMessageHelper(id, articleId, type, id1, id2, "FM"); }
        catch (Exception e) { }
    }

    private void addEmojiMessage(int id, int emojiId, int type, int id1, int id2) {
        try { addMessageHelper(id, emojiId, type, id1, id2, "EM"); }
        catch (Exception e) { }
    }

    private void sendMessage(int id) throws RelationNotFoundException, MessageIdNotFoundException, TagIdNotFoundException {
        network.sendMessage(id);
        network2.sendMessage(id);
        allAddedMessageIds.remove(id);
    }

    private void assertNetworkStructureConsistent() {
        PersonInterface[] persons1 = network.getPersons();
        PersonInterface[] persons2 = network2.getPersons();
        assertEquals(persons1.length, persons2.length);

        Arrays.sort(persons1, Comparator.comparingInt(PersonInterface::getId));
        Arrays.sort(persons2, Comparator.comparingInt(PersonInterface::getId));

        for (int i = 0; i < persons1.length; i++) {
            PersonInterface p1 = persons1[i];
            PersonInterface p2 = persons2[i];

            assertTrue(((Person) p1).strictEquals(p2));

            for (int otherId : allPersonIds) {
                if (p1.getId() != otherId) {
                    PersonInterface otherP1 = network.getPerson(otherId);
                    PersonInterface otherP2 = network2.getPerson(otherId);

                    boolean linked1 = false;
                    boolean linked2 = false;
                    try { linked1 = p1.isLinked(otherP1); } catch (Exception e) { fail(); }
                    try { linked2 = p2.isLinked(otherP2); } catch (Exception e) { fail(); }

                    assertEquals(linked1, linked2);

                    if (linked1) {
                        try {
                            int value1 = p1.queryValue(otherP1);
                            int value2 = p2.queryValue(otherP2);
                            assertEquals(value1, value2);
                        }
                        catch (Exception e) {
                            fail();
                        }
                    }
                }
            }

            for (int tagId : personOwnedTagIds.getOrDefault(p1.getId(), Collections.emptySet())) {
                boolean contains1 = false;
                boolean contains2 = false;
                try { contains1 = p1.containsTag(tagId); } catch (Exception e) { fail(); }
                try { contains2 = p2.containsTag(tagId); } catch (Exception e) { fail(); }

                assertEquals(contains1, contains2);
                if (contains1) {
                    TagInterface t1 = null;
                    TagInterface t2 = null;
                    try { t1 = p1.getTag(tagId); } catch (Exception e) { fail(); }
                    try { t2 = p2.getTag(tagId); } catch (Exception e) { fail(); }

                    assertNotNull(t1);
                    assertNotNull(t2);

                    try { assertEquals(t1.getSize(), t2.getSize()); } catch (Exception e) { fail(); }
                    try { assertEquals(t1.getValueSum(), t2.getValueSum()); } catch (Exception e) { fail(); }
                    try { assertEquals(t1.getAgeVar(), t2.getAgeVar()); } catch (Exception e) { fail(); }
                }
            }

            try { assertEquals(p1.getMoney(), p2.getMoney()); } catch (Exception e) { fail(); }
            try { assertEquals(p1.getSocialValue(), p2.getSocialValue()); } catch (Exception e) { fail(); }

            List<Integer> receivedArticles1 = null;
            List<Integer> receivedArticles2 = null;
            try { receivedArticles1 = p1.queryReceivedArticles(); } catch (Exception e) { fail(); }
            try { receivedArticles2 = p2.queryReceivedArticles(); } catch (Exception e) { fail(); }
            assertEquals(receivedArticles1, receivedArticles2);
        }
    }

    private void assertEmojiState(int[] expectedKeptIds, int[] expectedKeptHeats) {
        int[] actualIds = network.getEmojiIdList();
        int[] actualHeats = network.getEmojiHeatList();

        assertEquals(expectedKeptIds.length, actualIds.length);
        assertEquals(expectedKeptHeats.length, actualHeats.length);

        int[] sortedActualIds = Arrays.copyOf(actualIds, actualIds.length);
        Arrays.sort(sortedActualIds);
        int[] sortedExpectedIds = Arrays.copyOf(expectedKeptIds, expectedKeptIds.length);
        Arrays.sort(sortedExpectedIds);

        assertArrayEquals(sortedExpectedIds, sortedActualIds);

        Map<Integer, Integer> actualHeatMap = new HashMap<>();
        for(int i = 0; i < actualIds.length; i++) {
            actualHeatMap.put(actualIds[i], actualHeats[i]);
        }

        for(int i = 0; i < expectedKeptIds.length; i++) {
            int id = expectedKeptIds[i];
            int expectedHeat = expectedKeptHeats[i];
            assertTrue(actualHeatMap.containsKey(id));
            assertEquals(expectedHeat, actualHeatMap.get(id).intValue());
        }

        List<Integer> keptList = Arrays.stream(expectedKeptIds).boxed().collect(Collectors.toList());
        for (int originalId : allStoredEmojiIds) {
            if (!keptList.contains(originalId)) {
                try { network.queryPopularity(originalId); fail(); } catch (EmojiIdNotFoundException e) { }
            } else {
                try { network.queryPopularity(originalId); } catch (EmojiIdNotFoundException e) { fail(); }
            }
        }
    }

    private void assertMessagesState(Integer... expectedKeptMessageIds) {
        MessageInterface[] actualMessagesArray = network.getMessages();
        List<Integer> actualMessageIds = new ArrayList<>();
        for (MessageInterface msg : actualMessagesArray) {
            actualMessageIds.add(msg.getId());
        }
        Collections.sort(actualMessageIds);

        List<Integer> expectedMessageIdsList = Arrays.asList(expectedKeptMessageIds);
        Collections.sort(expectedMessageIdsList);

        assertEquals(expectedMessageIdsList, actualMessageIds);
    }

    private void assertReceivedMessages(int personId, Integer... expectedMessageIds) throws PersonIdNotFoundException {
        List<MessageInterface> received = network.queryReceivedMessages(personId);
        assertEquals(expectedMessageIds.length, received.size());

        List<Integer> actualReceivedIds = new ArrayList<>();
        for(MessageInterface msg : received) {
            actualReceivedIds.add(msg.getId());
        }

        List<Integer> expectedReceivedIdsList = Arrays.asList(expectedMessageIds);

        assertEquals(expectedReceivedIdsList, actualReceivedIds);
    }

    @Test
    public void test1() throws Exception {
        assertEmojiState(new int[]{}, new int[]{});
        assertMessagesState();
        assertNetworkStructureConsistent();

        int limit = 1;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(0, remaining);

        assertEmojiState(new int[]{}, new int[]{});
        assertMessagesState();
        assertNetworkStructureConsistent();
    }

    @Test
    public void test2() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        addTag(1, 101);
        addPersonToTag(2, 1, 101);
        createOfficialAccount(1, 1000, "OA1");
        contributeArticle(1, 1000, 5001);
        followOfficialAccount(2, 1000);

        assertEmojiState(new int[]{}, new int[]{});
        assertMessagesState();
        assertNetworkStructureConsistent();

        int limit = 5;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(0, remaining);

        assertEmojiState(new int[]{}, new int[]{});
        assertMessagesState();
        assertNetworkStructureConsistent();
    }

    @Test
    public void test3() throws Exception {
        addPerson(1, "A", 20);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        assertEmojiState(new int[]{101, 102, 103}, new int[]{0, 0, 0});
        assertMessagesState();
        assertNetworkStructureConsistent();

        int limit = 1;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(0, remaining);
        assertEmojiState(new int[]{}, new int[]{});
        assertMessagesState();
        assertNetworkStructureConsistent();

        setUp();
        addPerson(1, "A", 20);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        limit = 0;
        remaining = network.deleteColdEmoji(limit);
        assertEquals(3, remaining);
        assertEmojiState(new int[]{101, 102, 103}, new int[]{0, 0, 0});
        assertMessagesState();
        assertNetworkStructureConsistent();
    }

    @Test
    public void test4() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        addMessage(1001, 10, 0, 1, 2);
        addRedEnvelopeMessage(1002, 50, 0, 1, 2);
        addEmojiMessage(1003, 101, 0, 1, 2);
        addEmojiMessage(1004, 102, 0, 1, 2);

        assertEmojiState(new int[]{101, 102, 103}, new int[]{0, 0, 0});
        assertMessagesState(1001, 1002, 1003, 1004);
        assertNetworkStructureConsistent();

        int limit = 1;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(0, remaining);
        assertEmojiState(new int[]{}, new int[]{});
        assertMessagesState(1001, 1002);
        assertNetworkStructureConsistent();
    }

    @Test
    public void test5() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);
        storeEmojiId(104);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 101, 0, 1, 2); sendMessage(9002);
        addEmojiMessage(9003, 102, 0, 1, 2); sendMessage(9003);
        addEmojiMessage(9004, 103, 0, 1, 2); sendMessage(9004);
        addEmojiMessage(9005, 103, 0, 1, 2); sendMessage(9005);
        addEmojiMessage(9006, 103, 0, 1, 2); sendMessage(9006);

        addMessage(1001, 10, 0, 1, 2);
        addRedEnvelopeMessage(1002, 50, 0, 1, 2);
        addEmojiMessage(1003, 101, 0, 1, 2);
        addEmojiMessage(1004, 102, 0, 1, 2);
        addEmojiMessage(1005, 103, 0, 1, 2);
        addEmojiMessage(1006, 104, 0, 1, 2);

        assertEquals(2, network.queryPopularity(101));
        assertEquals(1, network.queryPopularity(102));
        assertEquals(3, network.queryPopularity(103));
        assertEquals(0, network.queryPopularity(104));
        assertEmojiState(new int[]{101, 102, 103, 104}, new int[]{2, 1, 3, 0});
        assertMessagesState(1001, 1002, 1003, 1004, 1005, 1006);

        int limit = 2;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(2, remaining);

        assertEmojiState(new int[]{101, 103}, new int[]{2, 3});
        assertMessagesState(1001, 1002, 1003, 1005);
        assertNetworkStructureConsistent();
    }

    @Test
    public void test6() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 102, 0, 1, 2); sendMessage(9002);
        addEmojiMessage(9003, 102, 0, 1, 2); sendMessage(9003);
        addEmojiMessage(9004, 103, 0, 1, 2); sendMessage(9004);
        addEmojiMessage(9005, 103, 0, 1, 2); sendMessage(9005);
        addEmojiMessage(9006, 103, 0, 1, 2); sendMessage(9006);

        addEmojiMessage(1001, 101, 0, 1, 2);
        addEmojiMessage(1002, 102, 0, 1, 2);
        addEmojiMessage(1003, 103, 0, 1, 2);
        addMessage(1004, 5, 0, 1, 2);

        assertEquals(1, network.queryPopularity(101));
        assertEquals(2, network.queryPopularity(102));
        assertEquals(3, network.queryPopularity(103));
        assertEmojiState(new int[]{101, 102, 103}, new int[]{1, 2, 3});
        assertMessagesState(1001, 1002, 1003, 1004);

        int limit = 2;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(2, remaining);

        assertEmojiState(new int[]{102, 103}, new int[]{2, 3});
        assertMessagesState(1002, 1003, 1004);
        assertNetworkStructureConsistent();
    }

    @Test
    public void test7() throws Exception {
        addPerson(1, "A", 25);
        addPerson(2, "B", 30);
        addPerson(3, "C", 28);
        addPerson(4, "D", 35);
        addPerson(5, "E", 22);

        addRelation(1, 2, 10);
        addRelation(1, 3, 5);
        addRelation(2, 3, 12);
        addRelation(4, 5, 8);

        addTag(1, 100);
        addPersonToTag(2, 1, 100);
        addPersonToTag(3, 1, 100);

        addTag(4, 200);
        addPersonToTag(5, 4, 200);

        createOfficialAccount(1, 1000, "OA1");
        contributeArticle(1, 1000, 5001);
        contributeArticle(1, 1000, 5002);
        followOfficialAccount(2, 1000);
        followOfficialAccount(3, 1000);

        createOfficialAccount(4, 2000, "OA2");
        contributeArticle(4, 2000, 5003);
        followOfficialAccount(5, 2000);
        contributeArticle(5, 2000, 5004);

        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);
        storeEmojiId(104);
        storeEmojiId(105);
        storeEmojiId(106);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 101, 0, 1, 3); sendMessage(9002);
        addEmojiMessage(9003, 101, 1, 1, 100); sendMessage(9003);

        addEmojiMessage(9010, 102, 0, 4, 5); sendMessage(9010);
        addEmojiMessage(9011, 102, 1, 4, 200); sendMessage(9011);

        addEmojiMessage(9020, 103, 0, 2, 3); sendMessage(9020);

        addEmojiMessage(9030, 104, 0, 5, 4); sendMessage(9030);
        addEmojiMessage(9031, 104, 0, 5, 4); sendMessage(9031);
        addEmojiMessage(9032, 104, 0, 5, 4); sendMessage(9032);

        addEmojiMessage(9040, 105, 1, 1, 100); sendMessage(9040);

        assertEquals(3, network.queryPopularity(101));
        assertEquals(2, network.queryPopularity(102));
        assertEquals(1, network.queryPopularity(103));
        assertEquals(3, network.queryPopularity(104));
        assertEquals(1, network.queryPopularity(105));
        assertEquals(0, network.queryPopularity(106));

        addMessage(10001, 10, 0, 1, 2);
        addRedEnvelopeMessage(10002, 100, 0, 2, 3);
        addForwardMessage(10003, 5001, 0, 1, 4);
        addForwardMessage(10004, 5003, 1, 4, 200);
        addEmojiMessage(10005, 101, 0, 1, 2);
        addEmojiMessage(10006, 102, 0, 2, 3);
        addEmojiMessage(10007, 103, 1, 1, 100);
        addEmojiMessage(10008, 104, 0, 4, 5);
        addEmojiMessage(10009, 105, 0, 5, 4);

        assertMessagesState(10001, 10002, 10003, 10004, 10005, 10006, 10007, 10008, 10009);

        int limit = 3;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(2, remaining);

        assertEmojiState(new int[]{101, 104}, new int[]{3, 3});
        try { network.queryPopularity(102); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(103); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(105); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(106); fail(); } catch (EmojiIdNotFoundException e) {}

        assertMessagesState(10001, 10002, 10003, 10004, 10005, 10008);

        assertNetworkStructureConsistent();

        assertReceivedMessages(1);
        assertReceivedMessages(2, 9040, 9003, 9001);
        assertReceivedMessages(3, 9040, 9020, 9003, 9002);
        assertReceivedMessages(4, 9032, 9031, 9030);
        assertReceivedMessages(5, 9011, 9010);
    }

    @Test
    public void test8() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        storeEmojiId(101);
        storeEmojiId(102);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 101, 0, 1, 2); sendMessage(9002);

        addEmojiMessage(9003, 102, 0, 1, 2); sendMessage(9003);

        addMessage(1001, 10, 0, 1, 2);
        addRedEnvelopeMessage(1002, 50, 0, 1, 2);

        assertEquals(2, network.queryPopularity(101));
        assertEquals(1, network.queryPopularity(102));
        assertEmojiState(new int[]{101, 102}, new int[]{2, 1});
        assertMessagesState(1001, 1002);
        assertReceivedMessages(2, 9003, 9002, 9001);

        int limit = 2;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(1, remaining);

        assertEmojiState(new int[]{101}, new int[]{2});
        try { network.queryPopularity(102); fail(); } catch (EmojiIdNotFoundException e) {}

        assertMessagesState(1001, 1002);
        assertReceivedMessages(2, 9003, 9002, 9001);
        assertNetworkStructureConsistent();
    }

    @Test
    public void test9() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 101, 0, 1, 2); sendMessage(9002);

        addEmojiMessage(1001, 101, 0, 1, 2);
        addEmojiMessage(1002, 102, 0, 1, 2);
        addEmojiMessage(1003, 103, 0, 1, 2);
        addMessage(1004, 5, 0, 1, 2);
        addRedEnvelopeMessage(1005, 60, 0, 1, 2);

        assertEmojiState(new int[]{101, 102, 103}, new int[]{2, 0, 0});
        assertMessagesState(1001, 1002, 1003, 1004, 1005);

        int limit = 1;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(1, remaining);

        assertEmojiState(new int[]{101}, new int[]{2});
        try { network.queryPopularity(102); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(103); fail(); } catch (EmojiIdNotFoundException e) {}

        assertMessagesState(1001, 1004, 1005);
        assertNetworkStructureConsistent();
    }

    @Test
    public void test10_NonEmojiMessagesKept() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addRelation(1, 2, 10);
        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 101, 0, 1, 2); sendMessage(9002);
        addEmojiMessage(9003, 101, 0, 1, 2); sendMessage(9003);
        addEmojiMessage(9004, 101, 0, 1, 2); sendMessage(9004);
        addEmojiMessage(9005, 101, 0, 1, 2); sendMessage(9005);

        addMessage(1001, 10, 0, 1, 2);
        addRedEnvelopeMessage(1002, 50, 0, 1, 2);
        createOfficialAccount(1, 1000, "OA");
        contributeArticle(1, 1000, 5001);
        addForwardMessage(1003, 5001, 0, 1, 2);

        addEmojiMessage(1004, 101, 0, 1, 2);
        addEmojiMessage(1005, 102, 0, 1, 2);
        addEmojiMessage(1006, 103, 0, 1, 2);

        assertEquals(5, network.queryPopularity(101));
        assertEquals(0, network.queryPopularity(102));
        assertEquals(0, network.queryPopularity(103));
        assertEmojiState(new int[]{101, 102, 103}, new int[]{5, 0, 0});
        assertMessagesState(1001, 1002, 1003, 1004, 1005, 1006);

        int limit = 1;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(1, remaining);

        assertEmojiState(new int[]{101}, new int[]{5});
        try { network.queryPopularity(102); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(103); fail(); } catch (EmojiIdNotFoundException e) {}

        assertMessagesState(1001, 1002, 1003, 1004);
        assertNetworkStructureConsistent();
    }

    @Test
    public void test12() throws Exception {
        addPerson(1, "A", 20);
        addPerson(2, "B", 21);
        addPerson(3, "C", 22);
        addRelation(1, 2, 10);
        addRelation(1, 3, 10);
        addRelation(2, 3, 10);
        addTag(1, 100);
        addPersonToTag(2, 1, 100);
        addPersonToTag(3, 1, 100);

        storeEmojiId(101);
        storeEmojiId(102);
        storeEmojiId(103);

        addEmojiMessage(9001, 101, 0, 1, 2); sendMessage(9001);
        addEmojiMessage(9002, 102, 1, 1, 100); sendMessage(9002);
        addEmojiMessage(9003, 103, 0, 2, 3); sendMessage(9003);
        addEmojiMessage(9004, 101, 0, 1, 3); sendMessage(9004);
        addEmojiMessage(9005, 102, 0, 1, 2); sendMessage(9005);
        addEmojiMessage(9006, 103, 1, 1, 100); sendMessage(9006);

        assertEquals(2, network.queryPopularity(101));
        assertEquals(2, network.queryPopularity(102));
        assertEquals(2, network.queryPopularity(103));
        assertEmojiState(new int[]{101, 102, 103}, new int[]{2, 2, 2});

        addEmojiMessage(10001, 101, 0, 1, 2);
        addEmojiMessage(10002, 102, 0, 1, 3);
        addEmojiMessage(10003, 103, 1, 1, 100);
        addMessage(10004, 5, 0, 1, 2);
        addRedEnvelopeMessage(10005, 50, 0, 1, 2);

        assertEmojiState(new int[]{101, 102, 103}, new int[]{2, 2, 2});
        assertMessagesState(10001, 10002, 10003, 10004, 10005);

        int limit = 3;
        int remaining = network.deleteColdEmoji(limit);
        assertEquals(0, remaining);

        assertEmojiState(new int[]{}, new int[]{});
        try { network.queryPopularity(101); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(102); fail(); } catch (EmojiIdNotFoundException e) {}
        try { network.queryPopularity(103); fail(); } catch (EmojiIdNotFoundException e) {}

        assertMessagesState(10004, 10005);
        assertNetworkStructureConsistent();

        assertReceivedMessages(1);
        assertReceivedMessages(2, 9006, 9005, 9002, 9001);
        assertReceivedMessages(3, 9006, 9004, 9003, 9002);
    }
}