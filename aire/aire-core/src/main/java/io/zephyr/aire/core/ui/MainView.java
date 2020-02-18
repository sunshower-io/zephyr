package io.zephyr.aire.core.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("whatever/world")
public class MainView extends VerticalLayout {

  public MainView() {
    add(new Button("Frapadap"));
    add(new Text("Hello World"));
  }
}
