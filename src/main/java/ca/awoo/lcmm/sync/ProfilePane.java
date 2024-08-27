package ca.awoo.lcmm.sync;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JPanel;

public class ProfilePane extends JPanel{
    private final List<ProfileWidget> profiles;
    private Optional<ProfileWidget> selected;

    public ProfilePane(){
        profiles = new ArrayList<>();
        this.setLayout(new FlowLayout());
        selected = Optional.empty();

    }

    public void addProfile(ProfileWidget profile){
        this.profiles.add(profile);
        this.add(profile);
        profile.setProfilePane(this);
        this.validate();
        this.repaint();
    }

    public void select(ProfileWidget newSelected) {
        if(selected.isPresent()){
            selected.get().setSelected(false);
        }
        newSelected.setSelected(true);
        this.selected = Optional.of(newSelected);
        notifyProfileChange(newSelected.getProfile());
    }

    public void deselect(){
        if(selected.isPresent()){
            selected.get().setSelected(false);
        }
        this.selected = Optional.empty();
        notifyProfileChange(null);
    }

    private Set<Consumer<Profile>> profileChangeListeners = new HashSet<>();

    public void listenProfileChange(Consumer<Profile> listener){
        profileChangeListeners.add(listener);
    }

    public void unlistenProfileChange(Consumer<Profile> listener){
        profileChangeListeners.remove(listener);
    }

    private void notifyProfileChange(Profile newProfile){
        for(Consumer<Profile> listener : profileChangeListeners){
            listener.accept(newProfile);
        }
    }
}
