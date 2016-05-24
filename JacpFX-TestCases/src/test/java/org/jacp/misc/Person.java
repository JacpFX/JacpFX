package org.jacp.misc;

import java.util.UUID;

/**
 * Created by Andy Moncsek on 24.05.16.
 */
public class Person implements Comparable<Person>{
    private String firstName;
    private String lastName;
    private UUID id;
    public Person(String firstName, String lastName, UUID id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
       return result;

       /** int p1 = firstName != null ? firstName.hashCode() : 0;
        int p2 = lastName != null ? lastName.hashCode() : 0;
        int p3 = id != null ? id.hashCode() : 0;


        return id.hashCode();**/
    }

    /**  @Override
    public int hashCode() {
        return 5;
    }  **/


    @Override
    public boolean equals(Object obj) {
        return this.id.compareTo(((Person)obj).id)==0;
    }

   @Override
    public int compareTo(Person person) {
        return this.id.compareTo(person.id);
    }
}
