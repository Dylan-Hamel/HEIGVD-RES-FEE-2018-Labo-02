package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.data.StudentsStoreImpl;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class implements the client side of the protocol specification (version 1).
 *
 * @author Olivier Liechti
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

    private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());

    private Socket curr_socket;
    private StudentsStoreImpl studentList = new StudentsStoreImpl();

    private OutputStream os = null;
    private InputStream is = null;

    private InfoCommandResponse info;

    @Override
    public void connect(String server, int port) throws IOException {
        try {
            curr_socket = new Socket(server,port);
            os = curr_socket.getOutputStream();
            is = curr_socket.getInputStream();
            getStringFromInputStream(is);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //Port non-ouvert ou non autorisé
        }
    }

    @Override
    public void disconnect() throws IOException {
        os.write((RouletteV1Protocol.CMD_BYE + "\n").getBytes());
        os.flush();
        os.close();
        is.close();
        curr_socket.close();
        info = null;
    }

    @Override
    public boolean isConnected() {
        if (curr_socket != null){
            return curr_socket.isConnected();
        }
        return false;
    }

    @Override
    public void loadStudent(String fullname) throws IOException {
        if (curr_socket != null && isConnected()) {
            os.write((RouletteV1Protocol.CMD_LOAD + "\n").getBytes());
            getStringFromInputStream(is);
            os.write(fullname.getBytes());
            os.write("\n".getBytes());
            os.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + "\n").getBytes());
            getStringFromInputStream(is);
        }
        else{
            throw new IOException();
        }
    }

    @Override
    public void loadStudents(List<Student> students) throws IOException {
        if (curr_socket != null && isConnected()) {
            os.write((RouletteV1Protocol.CMD_LOAD + "\n").getBytes());
            getStringFromInputStream(is);
            for (Student stu : students) {
                os.write(stu.getFullname().getBytes());
                os.write('\n');
            }
            os.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + "\n").getBytes());
            getStringFromInputStream(is);
        }
        else{
            throw new IOException();
        }
    }

    @Override
    public Student pickRandomStudent() throws EmptyStoreException, IOException {
        // use JsonObjectMapper.parseJson(json, Student.class);
        if (curr_socket != null && isConnected()) {
            os.write((RouletteV1Protocol.CMD_RANDOM + "\n").getBytes());
            String test = getStringFromInputStream(is);
            if (test.contains("no student")){
                throw new EmptyStoreException();
            }
            RandomCommandResponse new_one = JsonObjectMapper.parseJson(test, RandomCommandResponse.class);
            if(!new_one.getError().isEmpty()){
                throw new EmptyStoreException();
            }

            return new Student(new_one.getFullname());
        }
        return null;
    }

    @Override
    public int getNumberOfStudents() throws IOException {
        if (curr_socket != null && isConnected()){
            os.write((RouletteV1Protocol.CMD_INFO + "\n").getBytes());
            String test = getStringFromInputStream(is);
            info = JsonObjectMapper.parseJson(test, InfoCommandResponse.class);;
            return info.getNumberOfStudents();
        }
        else{
            throw new IOException();
        }
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
