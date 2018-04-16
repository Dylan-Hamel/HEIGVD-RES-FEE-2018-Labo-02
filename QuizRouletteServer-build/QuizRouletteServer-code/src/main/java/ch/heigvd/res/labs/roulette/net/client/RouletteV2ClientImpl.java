package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.data.StudentsList;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV2Protocol;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.net.protocol.WrapperJSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class implements the client side of the protocol specification (version 2).
 *
 * @author Olivier Liechti
 */
public class RouletteV2ClientImpl extends RouletteV1ClientImpl implements IRouletteV2Client {

  @Override
  public void clearDataStore() throws IOException {
    this.os.write((RouletteV2Protocol.CMD_CLEAR  + "\n").getBytes());
    this.os.flush();
    // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<Student> listStudents() throws IOException {
    this.os.write((RouletteV2Protocol.CMD_LIST +  "\n").getBytes());
    this.os.flush();
    byte[] b = new byte[255];
    WrapperJSON wj = new WrapperJSON();

    this.is.read(b);
    String text = new String(b);

    wj = JsonObjectMapper.parseJson(text, WrapperJSON.class);
    return wj.getStudents();
    // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String getProtocolVersion() throws IOException {
    return RouletteV2Protocol.VERSION;
  }
  
}
