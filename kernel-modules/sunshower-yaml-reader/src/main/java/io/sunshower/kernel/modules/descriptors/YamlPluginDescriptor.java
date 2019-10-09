package io.sunshower.kernel.modules.descriptors;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.PluginDescriptor;
import io.sunshower.kernel.graph.Dependency;
import lombok.Getter;

import java.util.List;

@Getter
public class YamlPluginDescriptor implements PluginDescriptor {


    public String name;

    public String group;

    public String version;

    public List<YamlDependency> dependencies;


    public static class YamlDependency implements Dependency {

        public boolean required;

        public String group;

        public String artifact;

        public String version;

        private Coordinate coordinate;

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public Coordinate getCoordinate() {
            if (coordinate == null) {
                coordinate = new Coordinate(group, artifact, version);
            }
            return coordinate;
        }
    }
}
