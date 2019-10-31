package io.sunshower.kernel.state.xml;

import com.thoughtworks.xstream.XStream;
import io.sunshower.kernel.state.Caretaker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.val;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class RecursiveMementoContext implements Caretaker {

  final File destination;

  public RecursiveMementoContext(final File destination) {
    this.destination = destination;
  }

  @Override
  public <T> void save(T t) {
    try (val fis = new FileOutputStream(destination)) {
      val xstream = new XStream();
      xstream.toXML(t, fis);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T restore(Class<T> type) {
    try (val fis = new FileInputStream(destination)) {
      val xstream = new XStream();
      return (T) xstream.fromXML(fis);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
