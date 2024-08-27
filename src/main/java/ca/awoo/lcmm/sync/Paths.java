package ca.awoo.lcmm.sync;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Arrays;
import java.util.Stack;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class Paths {

    public static final Path root = new NormalPath(true, new String[0]);

    public static class NormalPath implements Path {
        private final boolean absolute;
        private final String[] names;

        public NormalPath(boolean absolute, String... names){
            this.absolute = absolute;
            this.names = names;
        }

        @Override
        public FileSystem getFileSystem() {
            return null;
        }

        @Override
        public boolean isAbsolute() {
            return absolute;
        }

        @Override
        public Path getRoot() {
            return absolute ? root : null;
        }

        @Override
        public Path getFileName() {
            return new NormalPath(false, names[names.length - 1]);
        }

        @Override
        public Path getParent() {
            return new NormalPath(absolute, Arrays.copyOfRange(names, 0, names.length - 1));
        }

        @Override
        public int getNameCount() {
            return names.length;
        }

        @Override
        public Path getName(int index) {
            if(index < 0) index += names.length;
            return new NormalPath(false, names[index]);
        }

        @Override
        public Path subpath(int beginIndex, int endIndex) {
            return new NormalPath(false, Arrays.copyOfRange(names, beginIndex, endIndex));
        }

        @Override
        public boolean startsWith(Path other) {
            if(other.isAbsolute() == absolute && other.getNameCount() <= names.length){
                for(int i = 0; i < other.getNameCount(); i++){
                    if(!other.getName(i).toString().equals(names[i])){
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean endsWith(Path other) {
            if(other.isAbsolute()){
                return this.equals(other);
            }else if(other.getNameCount() > names.length){
                return false;
            }else{
                for(int i = -1; i >= -other.getNameCount(); i--){
                    if(!this.getName(i).toString().equals(other.getName(i).toString())){
                        return false;
                    }
                }
                return true;
            }
        }

        @Override
        public Path normalize() {
            Stack<String> normalNames = new Stack<>();
            for(String name : names){
                if(name.equals("..")){
                    normalNames.pop();
                }else if(!name.equals(".")){
                    normalNames.push(name);
                }
            }
            return new NormalPath(absolute, normalNames.toArray(new String[0]));
        }

        @Override
        public Path resolve(Path other) {
            if(other.isAbsolute()) return other;
            Stack<String> newNames = new Stack<>();
            for(String name : names){
                newNames.push(name);
            }
            for(int i = 0; i < other.getNameCount(); i++){
                newNames.push(other.getName(i).toString());
            }
            return new NormalPath(absolute, newNames.toArray(new String[0]));
        }

        @Override
        public Path relativize(Path other) {
            if(other.isAbsolute() != absolute){
                throw new IllegalArgumentException("Only one path is absolute");
            }
            if(!other.startsWith(this)){
                throw new IllegalArgumentException("Other does not start with this");
            }
            Stack<String> newNames = new Stack<>();
            for(int i = names.length; i < other.getNameCount(); i++){
                newNames.push(other.getName(i).toString());
            }
            return new NormalPath(false, newNames.toArray(new String[0]));
        }

        @Override
        public URI toUri() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'toUri'");
        }

        @Override
        public Path toAbsolutePath() {
            if(absolute) return this;
            throw new UnsupportedOperationException("This doesn't do that");
        }

        @Override
        public Path toRealPath(LinkOption... options) throws IOException {
            return this;
        }

        @Override
        public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
            return null;
        }

        @Override
        public int compareTo(Path other) {
            return this.toString().compareTo(other.toString());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (absolute ? 1231 : 1237);
            result = prime * result + Arrays.hashCode(names);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NormalPath other = (NormalPath) obj;
            if (absolute != other.absolute)
                return false;
            if (!Arrays.equals(names, other.names))
                return false;
            return true;
        }

        @Override
        public String toString() {
            if(!absolute && names.length == 1) return names[0];
            StringBuilder sb = new StringBuilder();
            if(absolute) sb.append("/");
            for(String name : names){
                sb.append(name);
                sb.append("/");
            }
            if(names.length > 0){
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    public static Path get(String path){
        if(path.equals("/")) return root;
        if(path.isEmpty()) return new NormalPath(false, new String[0]);
        boolean absolute = path.startsWith("/");
        String[] parts = path.substring(absolute ? 1 : 0).split("/");
        return new NormalPath(absolute, parts);
    }

    public static Path get(String start, String... parts){
        boolean absolute = start.startsWith("/");
        Stack<String> names = new Stack<>();
        String[] startParts = start.substring(absolute ? 1 : 0).split("/");
        for(String startPart : startParts){
            names.push(startPart);
        }
        for(String part : parts){
            String[] partParts = part.split("/");
            for(String partPart : partParts){
                names.push(partPart);
            }
        }
        return new NormalPath(absolute, names.toArray(new String[0]));
    }
}
