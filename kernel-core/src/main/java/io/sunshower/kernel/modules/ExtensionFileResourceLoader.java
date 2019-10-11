package io.sunshower.kernel.modules;

import static java.security.AccessController.doPrivileged;

import io.sunshower.common.io.IO;
import java.io.*;
import java.net.*;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.val;
import org.jboss.modules.*;

public class ExtensionFileResourceLoader extends AbstractResourceLoader implements ResourceLoader {

  private static final String INDEX_FILE = "META-INF/PATHS.LIST";

  private final JarFile jarFile;
  private final String rootName;
  private final URL rootUrl;
  private final String relativePath;
  private final File fileOfJar;
  private volatile List<String> directory;

  // protected by {@code this}
  private final Map<CodeSigners, CodeSource> codeSources = new HashMap<>();

  ExtensionFileResourceLoader(final String rootName, final JarFile jarFile) {
    this(rootName, jarFile, null);
  }

  ExtensionFileResourceLoader(
      final String rootName, final JarFile jarFile, final String relativePath) {
    if (jarFile == null) {
      throw new IllegalArgumentException("jarFile is null");
    }
    if (rootName == null) {
      throw new IllegalArgumentException("rootName is null");
    }
    fileOfJar = new File(jarFile.getName());
    this.jarFile = jarFile;
    this.rootName = rootName;
    String realPath = relativePath == null ? null : PathUtils.canonicalize(relativePath);
    if (realPath != null && realPath.endsWith("/"))
      realPath = realPath.substring(0, realPath.length() - 1);
    this.relativePath = realPath;
    try {
      rootUrl = getJarURI(fileOfJar.toURI(), realPath).toURL();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid root file specified", e);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid root file specified", e);
    }
  }

  private static URI getJarURI(final URI original, final String nestedPath)
      throws URISyntaxException {
    val b = new StringBuilder();
    b.append("file:");
    assert original.getScheme().equals("file");
    final String path = original.getPath();
    assert path != null;
    val host = original.getHost();
    if (host != null) {
      final String userInfo = original.getRawUserInfo();
      b.append("//");
      if (userInfo != null) {
        b.append(userInfo).append('@');
      }
      b.append(host);
    }
    b.append(path).append("!/");
    if (nestedPath != null) {
      b.append(nestedPath);
    }
    return new URI("jar", b.toString(), null);
  }

  public String getRootName() {
    return rootName;
  }

  public synchronized ClassSpec getClassSpec(final String fileName) throws IOException {
    val spec = new ClassSpec();
    val entry = getJarEntry(fileName);
    if (entry == null) {
      // no such entry
      return null;
    }
    val size = entry.getSize();
    try (final InputStream is = jarFile.getInputStream(entry)) {
      if (size == -1) {
        // size unknown
        val baos = new ByteArrayOutputStream();
        val buf = new byte[16384];
        int res;
        while ((res = is.read(buf)) > 0) {
          baos.write(buf, 0, res);
        }
        // done
        var codeSource = createCodeSource(entry);
        baos.close();
        is.close();
        spec.setBytes(baos.toByteArray());
        spec.setCodeSource(codeSource);
        return spec;
      } else if (size <= (long) Integer.MAX_VALUE) {
        val castSize = (int) size;
        val bytes = new byte[castSize];
        int a = 0, res;
        while ((res = is.read(bytes, a, castSize - a)) > 0) {
          a += res;
        }
        // consume remainder so that cert check doesn't fail in case of wonky JARs
        while (is.read() != -1) {
          //
        }
        // done
        CodeSource codeSource = createCodeSource(entry);
        is.close();
        spec.setBytes(bytes);
        spec.setCodeSource(codeSource);
        return spec;
      } else {
        throw new IOException("Resource is too large to be a valid class file");
      }
    }
  }

  // this MUST only be called after the input stream is fully read (see MODULES-201)
  private CodeSource createCodeSource(final JarEntry entry) {
    val entryCodeSigners = entry.getCodeSigners();
    val codeSigners =
        entryCodeSigners == null || entryCodeSigners.length == 0
            ? EMPTY_CODE_SIGNERS
            : new CodeSigners(entryCodeSigners);
    var codeSource = codeSources.get(codeSigners);
    if (codeSource == null) {
      codeSources.put(codeSigners, codeSource = new CodeSource(rootUrl, entryCodeSigners));
    }
    return codeSource;
  }

  private JarEntry getJarEntry(final String fileName) {
    return relativePath == null
        ? jarFile.getJarEntry(fileName)
        : jarFile.getJarEntry(relativePath + "/" + fileName);
  }

  public PackageSpec getPackageSpec(final String name) throws IOException {
    final Manifest manifest;
    if (relativePath == null) {
      manifest = jarFile.getManifest();
    } else {
      val jarEntry = getJarEntry("META-INF/MANIFEST.MF");
      if (jarEntry == null) {
        manifest = null;
      } else {
        try (final InputStream inputStream = jarFile.getInputStream(jarEntry)) {
          manifest = new Manifest(inputStream);
        }
      }
    }
    return getPackageSpec(name, manifest, rootUrl);
  }

  public String getLibrary(final String name) {
    // JARs cannot have libraries in them
    return null;
  }

  public Resource getResource(String name) {
    try {
      val jarFile = this.jarFile;
      name = PathUtils.canonicalize(PathUtils.relativize(name));
      val entry = getJarEntry(name);
      if (entry == null) {
        return null;
      }
      final URI uri;
      try {
        val absoluteFile = new File(jarFile.getName()).getAbsoluteFile();
        var path = absoluteFile.getPath();
        path = PathUtils.canonicalize(path);
        if (File.separatorChar != '/') {
          // optimizes away on platforms with /
          path = path.replace(File.separatorChar, '/');
        }
        if (PathUtils.isRelative(path)) {
          // should not be possible, but the JDK thinks this might happen sometimes..?
          path = "/" + path;
        }
        if (path.startsWith("//")) {
          // UNC path URIs have loads of leading slashes
          path = "//" + path;
        }
        uri = new URI("file", null, path, null);
      } catch (URISyntaxException x) {
        throw new IllegalStateException(x);
      }
      final URL url =
          new URL(null, getJarURI(uri, entry.getName()).toString(), (URLStreamHandler) null);
      try {
        doPrivileged(new OpenURLConnectionAction(url));
      } catch (PrivilegedActionException e) {
        // ignored; the user might not even ask for the URL
      }
      return new EntryResource(jarFile, entry.getName(), relativePath, url);
    } catch (MalformedURLException e) {
      // must be invalid...?  (todo: check this out)
      return null;
    } catch (URISyntaxException e) {
      // must be invalid...?  (todo: check this out)
      return null;
    }
  }

  public Iterator<Resource> iterateResources(String startPath, final boolean recursive) {
    if (relativePath != null)
      startPath = startPath.equals("") ? relativePath : relativePath + "/" + startPath;
    val startName = PathUtils.canonicalize(PathUtils.relativize(startPath));
    var directory = this.directory;
    if (directory == null) {
      synchronized (jarFile) {
        directory = this.directory;
        if (directory == null) {
          directory = new ArrayList<>();
          final Enumeration<JarEntry> entries = jarFile.entries();
          while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();
            if (!jarEntry.isDirectory()) {
              directory.add(jarEntry.getName());
            }
          }
          this.directory = directory;
        }
      }
    }
    final Iterator<String> iterator = directory.iterator();
    return new Iterator<Resource>() {
      private Resource next;

      public boolean hasNext() {
        while (next == null) {
          if (!iterator.hasNext()) {
            return false;
          }
          final String name = iterator.next();
          if ((recursive
              ? PathUtils.isChild(startName, name)
              : PathUtils.isDirectChild(startName, name))) {
            try {
              next =
                  new EntryResource(
                      jarFile,
                      name,
                      relativePath,
                      getJarURI(new File(jarFile.getName()).toURI(), name).toURL());
            } catch (Exception ignored) {
            }
          }
        }
        return true;
      }

      public Resource next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        try {
          return next;
        } finally {
          next = null;
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public Collection<String> getPaths() {
    val index = new HashSet<String>();
    index.add("");
    val relativePath = this.relativePath;
    // First check for an external index
    val jarFile = this.jarFile;
    // Next check for an internal index
    val listEntry = jarFile.getJarEntry(INDEX_FILE);
    if (listEntry != null) {
      try {
        return readIndex(jarFile.getInputStream(listEntry), index, relativePath);
      } catch (IOException e) {
        index.clear();
      }
    }
    // Next just read the JAR
    extractJarPaths(jarFile, relativePath, index);
    return index;
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      try {
        jarFile.close();
      } catch (IOException e) {
        // ignored
      }
    }
  }

  public URI getLocation() {
    try {
      return getJarURI(fileOfJar.toURI(), "");
    } catch (URISyntaxException e) {
      return null;
    }
  }

  public ResourceLoader createSubloader(final String relativePath, final String rootName) {
    final String ourRelativePath = this.relativePath;
    final String fixedPath = PathUtils.relativize(PathUtils.canonicalize(relativePath));
    return new ExtensionFileResourceLoader(
        rootName, jarFile, ourRelativePath == null ? fixedPath : ourRelativePath + "/" + fixedPath);
  }

  static void extractJarPaths(
      final JarFile jarFile, String relativePath, final Collection<String> index) {
    index.add("");
    val entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      val jarEntry = entries.nextElement();
      val name = jarEntry.getName();
      val idx = name.lastIndexOf('/');
      if (idx == -1) continue;
      final String path = name.substring(0, idx);
      if (path.length() == 0 || path.endsWith("/")) {
        // invalid name, just skip...
        continue;
      }
      if (relativePath == null) {
        index.add(path);
      } else {
        if (path.startsWith(relativePath + "/")) {
          index.add(path.substring(relativePath.length() + 1));
        }
      }
    }
  }

  static void writeExternalIndex(final File indexFile, final Collection<String> index) {
    // Now try to write it
    boolean ok = false;
    try {
      try (val writer =
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexFile)))) {
        for (String name : index) {
          writer.write(name);
          writer.write('\n');
        }
        writer.close();
        ok = true;
      }
    } catch (IOException e) {
      // failed, ignore
    } finally {
      if (!ok) {
        // well, we tried...
        indexFile.delete();
      }
    }
  }

  static Collection<String> readIndex(
      final InputStream stream, final Collection<String> index, final String relativePath)
      throws IOException {
    val r = new BufferedReader(new InputStreamReader(stream));
    try {
      String s;
      while ((s = r.readLine()) != null) {
        String name = s.trim();
        if (relativePath == null) {
          index.add(name);
        } else {
          if (name.startsWith(relativePath + "/")) {
            index.add(name.substring(relativePath.length() + 1));
          }
        }
      }
      return index;
    } finally {
      // if exception is thrown, undo index creation
      r.close();
    }
  }

  static void addInternalIndex(File file, boolean modify) throws IOException {
    try (val oldJarFile = new JarFile(file, false)) {
      val index = new TreeSet<String>();
      val outputFile = new File(file.getAbsolutePath().replace(".jar", "-indexed.jar"));

      try (final ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(outputFile))) {
        Enumeration<JarEntry> entries = oldJarFile.entries();
        while (entries.hasMoreElements()) {
          final JarEntry entry = entries.nextElement();

          // copy data, unless we're replacing the index
          if (!entry.getName().equals(INDEX_FILE)) {
            final JarEntry clone = (JarEntry) entry.clone();
            // Compression level and format can vary across implementations
            if (clone.getMethod() != ZipEntry.STORED) clone.setCompressedSize(-1);
            zo.putNextEntry(clone);
            IO.copy(oldJarFile.getInputStream(entry), zo);
          }

          // add to the index
          val name = entry.getName();
          val idx = name.lastIndexOf('/');
          if (idx == -1) continue;
          final String path = name.substring(0, idx);
          if (path.length() == 0 || path.endsWith("/")) {
            // invalid name, just skip...
            continue;
          }
          index.add(path);
        }

        // write index
        zo.putNextEntry(new ZipEntry(INDEX_FILE));
        try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zo))) {
          for (final String name : index) {
            writer.write(name);
            writer.write('\n');
          }
        }
        zo.close();
        oldJarFile.close();

        if (modify) {
          file.delete();
          if (!outputFile.renameTo(file)) {
            throw new IOException(
                "failed to rename "
                    + outputFile.getAbsolutePath()
                    + " to "
                    + file.getAbsolutePath());
          }
        }
      }
    }
  }

  private static final CodeSigners EMPTY_CODE_SIGNERS = new CodeSigners(new CodeSigner[0]);

  static final class CodeSigners {

    private final CodeSigner[] codeSigners;
    private final int hashCode;

    CodeSigners(final CodeSigner[] codeSigners) {
      this.codeSigners = codeSigners;
      hashCode = Arrays.hashCode(codeSigners);
    }

    public boolean equals(final Object obj) {
      return obj instanceof CodeSigners && equals((CodeSigners) obj);
    }

    private boolean equals(final CodeSigners other) {
      return Arrays.equals(codeSigners, other.codeSigners);
    }

    public int hashCode() {
      return hashCode;
    }
  }
}
