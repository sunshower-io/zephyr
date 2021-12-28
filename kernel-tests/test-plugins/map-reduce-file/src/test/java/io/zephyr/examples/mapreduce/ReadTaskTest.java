package io.zephyr.examples.mapreduce;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.Tasks;
import io.zephyr.kernel.concurrency.TopologyAwareParallelScheduler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ReadTaskTest {

  static final String file = "/home/josiah/Downloads/prg/inputfile";

  @Test
  @SneakyThrows
  void ensureMappingFileToSegmentsWorks() {
    final int count = 32;
    val proc = Tasks.newProcess("mapreduce").parallel().coalesce();
    val collectTask = new Task("compute-segments") {
      @Override
      public TaskValue run(Scope scope) {
        val segs = computeSegments(count);
        scope.set("computed-segments", segs);
        return new TaskValue(segs, "computed-segments");
      }
    };
    proc.register(collectTask);

    val reducer = new ReducerTask();
    proc.register(reducer);
    for (int i = 0; i < count; i++) {
      val task = new WordCountTask(i);
      proc.register(task).dependsOn("compute-segments");
      proc.task().task("reducer").dependsOn(task);
    }


    val scheduler = new TopologyAwareParallelScheduler<String>(
        new ExecutorWorkerPool(Executors.newFixedThreadPool(count),
            Executors.newFixedThreadPool(2)));
    val process = proc.create();
    val result = scheduler.submit(process, Scope.root()).toCompletableFuture().toCompletableFuture().join();
    System.out.println(result);

  }

  @SneakyThrows
  private List<Segment> computeSegments(int count) {
    val segments = new ArrayList<Segment>();
    try (val rafile = new RandomAccessFile(new File(file), "r")) {
      val testSegmentSize = rafile.length() / count;
      for (int i = 0; i < count; i++) {
        segments.add(computeSegment(testSegmentSize, rafile, i, segments));
      }
    }
    return Collections.synchronizedList(segments);
  }

  private Segment computeSegment(long testSegmentSize, RandomAccessFile rafile,
      int i, List<Segment> segments) throws IOException {
    rafile.seek(i * testSegmentSize);
    long count = 0;// number of bits offset
    for (; ; ) {
      int r = rafile.read();
      count++;
      if (r == -1 || Character.isWhitespace(r)) {
        if (segments.isEmpty()) {
          return new Segment(i, testSegmentSize + count, 0);
        } else {
          val seg = segments.get(segments.size() - 1);
          return new Segment(i, (i + 1) * testSegmentSize + count, seg.end + 1);
        }
      }
    }
  }

  static class ReducerTask extends Task {

    public ReducerTask() {
      super("reducer");
    }

    @Override
    public TaskValue run(Scope scope) {
      System.out.println("DONE");
      System.out.println(scope);
      return null;
    }
  }

  static class WordCountTask extends Task {

    private final int count;
    private Segment segment;

    public WordCountTask(int count) {
      super("word-count" + count);
      this.count = count;
    }

    synchronized Segment getSegment(Scope scope) {
      List<Segment> segments = scope.get("computed-segments");
      return segments.remove(segments.size() - 1);
    }

    @Override
    public TaskValue run(Scope scope) {
      this.segment = getSegment(scope);

      val counts = new HashMap<>();
      try (val file = new RandomAccessFile(new File(ReadTaskTest.file), "r")) {
        file.seek(segment.start);
        val fis = new FileInputStream(file.getFD());
        val bis = new BufferedInputStream(fis);
        var count = segment.start;
        var buffer = new StringBuilder();

        while (true) {
          val ch = bis.read();
          if (ch == -1 || count == segment.end) {
            val word = buffer.toString().trim();
            counts.compute(word, (k, v) -> v == null ? 1 : (int) v + 1);
            scope.set(getName() + "values", counts);
            return null;
          }
          if(Character.isWhitespace((char) ch)) {
            val word = buffer.toString().trim();
            counts.compute(word, (k, v) -> v == null ? 1 : (int) v + 1);
            buffer = new StringBuilder();
          }
          count++;
          buffer.append((char) ch);
        }


      } catch (IOException ex) {
        System.out.println("Whoops: " + ex.getMessage());
      }
      return new TaskValue(null, getName());
    }
  }

  @ToString
  @AllArgsConstructor
  static final class Segment {

    int index;
    long end;
    long start;
  }

}