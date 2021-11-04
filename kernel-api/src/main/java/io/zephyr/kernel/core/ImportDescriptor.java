package io.zephyr.kernel.core;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class ImportDescriptor {


  @NonNull
  private final List<Path> includes;

  @NonNull
  private final List<Path> excludes;

}
