package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.StudentsStoreImpl;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class implements the client side of the protocol specification (version 1).
 *
 * @author Olivier Liechti
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

  private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());

  private Socket current_socket;
  private StudentsStoreImpl studentList = new StudentsStoreImpl();

  private OutputStream os = null;
  private InputStream is = null;

  @Override
  public void connect(String server, int port) throws IOException {
    try {
      current_socket = new Socket(server,port);
      os = current_socket.getOutputStream();
      is = current_socket.getInputStream();
      getStringFromInputStream(is);
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      //Port non-ouvert ou non autorisé
    }
  }

  @Override
  public void disconnect() throws IOException {
    os.flush();
    os.close();
    is.close();
    current_socket.close();
  }

  @Override
  public boolean isConnected() {
    if (current_socket != null){
      return current_socket.isConnected();
    }
    return false;
  }

  @Override
  public void loadStudent(String fullname) throws IOException {
    if (current_socket != null) {
      os.write("LOAD\n".getBytes());
      getStringFromInputStream(is);
      os.write(fullname.getBytes());
      os.write("\n".getBytes());
      os.write("ENDOFDATA\n".getBytes());
      getStringFromInputStream(is);
    }
  }

  @Override
  public void loadStudents(List<Student> students) throws IOException {
    if (current_socket != null) {
      os.write("LOAD\n".getBytes());
      getStringFromInputStream(is);
      for (Student stu : students) {
        os.write(stu.getFullname().getBytes());
        os.write('\n');
      }
      os.write("ENDOFDATA\n".getBytes());
      getStringFromInputStream(is);
    }
  }

  @Override
  public Student pickRandomStudent() throws EmptyStoreException, IOException {
    // use JsonObjectMapper.parseJson(json, Student.class);
    if (current_socket != null) {
      Student new_one;
      os.write("RANDOM\n".getBytes());
      String test = getStringFromInputStream(is);
      if (test.contains("no student")){
        throw new EmptyStoreException();
      }
      new_one = JsonObjectMapper.parseJson(test, Student.class);
      return new_one;
    }
    return null;
  }

  @Override
  public int getNumberOfStudents() throws IOException {
    if (current_socket != null){
      os.write("INFO\n".getBytes());
      String test = getStringFromInputStream(is);
      return Integer.parseInt(test.substring(test.lastIndexOf(":")+1,test.lastIndexOf("}")));
    }
    return 0;
  }

  @Override
  public String getProtocolVersion() throws IOException {
    return RouletteV1Protocol.VERSION;
  }

  private String getStringFromInputStream(InputStream is){

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder out = new StringBuilder();
    String line;
    try {
      if ((line = reader.readLine()) != null) {
        out.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return out.toString();
  }

}
