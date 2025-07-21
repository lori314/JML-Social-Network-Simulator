import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.ArrayList;

public class Tag implements TagInterface {
    private final int id;
    private final ArrayList<PersonInterface> persons;
    private int valueSum;

    public Tag(int id) {
        this.id = id;
        this.persons = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            Tag tag = (Tag) obj;
            return this.id == tag.getId();
        }
        return false;
    }

    public void addPerson(PersonInterface person) {
        if (person != null && !hasPerson(person)) {
            for (PersonInterface existingPerson : persons) {
                if (existingPerson.isLinked(person)) {
                    valueSum += person.queryValue(existingPerson);
                    valueSum += existingPerson.queryValue(person);
                }
            }
            persons.add(person);
        }
    }

    public boolean hasPerson(PersonInterface person) {
        for (PersonInterface p : persons) {
            if (p.equals(person)) {
                return true;
            }
        }
        return false;
    }

    public int getValueSum() {
        return valueSum;
    }

    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (PersonInterface p : persons) {
            sum += p.getAge();
        }
        return sum / persons.size();
    }

    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }
        int sum = 0;
        int mean = getAgeMean();
        for (PersonInterface p : persons) {
            sum += (p.getAge() - mean) * (p.getAge() - mean);
        }
        return sum / persons.size();
    }

    public void delPerson(PersonInterface person) {
        if (hasPerson(person)) {
            for (PersonInterface remainingPerson : persons) {
                if (!remainingPerson.equals(person) && person.isLinked(remainingPerson)) {
                    valueSum -= person.queryValue(remainingPerson);
                    valueSum -= remainingPerson.queryValue(person);
                }
            }
            persons.remove(person);
        }
    }

    public int getSize() {
        return persons.size();
    }

    ArrayList<PersonInterface> getPersons() {
        return persons;
    }

    void updateTagValueSum(int value) {
        valueSum += value;
    }
}
