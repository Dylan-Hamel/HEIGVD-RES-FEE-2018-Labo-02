package ch.heigvd.res.labs.roulette.net.protocol;
import ch.heigvd.res.labs.roulette.data.Student;
import java.util.List;

public class WrapperJSON {
    private List<Student> students;

    public WrapperJSON() {

    }

    public WrapperJSON(List<Student> students) {
        this.students = students;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
