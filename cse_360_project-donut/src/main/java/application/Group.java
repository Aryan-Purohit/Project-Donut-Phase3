package application;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group in the system, which can be a general group or a special access group.
 * Each group maintains lists of admins, instructors, and students.
 */
public class Group {

    private String groupName;
    private boolean isSpecialAccess;
    private List<User> admins;
    private List<User> instructors;
    private List<User> students;

    // Constructor
    public Group(String groupName, boolean isSpecialAccess) {
        this.groupName = groupName;
        this.isSpecialAccess = isSpecialAccess;
        this.admins = new ArrayList<>();
        this.instructors = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    // Getters
    public String getGroupName() {
        return groupName;
    }

    public boolean isSpecialAccess() {
        return isSpecialAccess;
    }

    public List<User> getAdmins() {
        return admins;
    }

    public List<User> getInstructors() {
        return instructors;
    }

    public List<User> getStudents() {
        return students;
    }

    // Methods to add a user to the group
    public boolean addUser(User user) {
        String role = user.getRole();
        if ("Admin".equalsIgnoreCase(role)) {
            if (!admins.contains(user)) {
                admins.add(user);
                user.addGroupName(groupName);
                return true;
            }
        } else if ("Instructor".equalsIgnoreCase(role)) {
            if (!instructors.contains(user)) {
                instructors.add(user);
                user.addGroupName(groupName);
                // First instructor added gets admin rights
                if (admins.isEmpty()) {
                    admins.add(user);
                    System.out.println("First instructor added as admin to group: " + groupName);
                }
                return true;
            }
        } else if ("Student".equalsIgnoreCase(role)) {
            if (!students.contains(user)) {
                students.add(user);
                user.addGroupName(groupName);
                return true;
            }
        }
        return false;
    }

    // Methods to remove a user from the group
    public boolean removeUser(User user) {
        boolean removed = false;
        if (admins.remove(user)) {
            removed = true;
        }
        if (instructors.remove(user)) {
            removed = true;
        }
        if (students.remove(user)) {
            removed = true;
        }
        if (removed) {
            user.removeGroupName(groupName);
        }
        return removed;
    }
}
