import com.mpatric.mp3agic.ID3v24Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArtistName {
    private String artistName;
    private List<String> songNames;
    private List<String> artistNameList;

    /**Finds the artists name and adds a String list with all his Tracks  */
    public ArtistName(){
        File file = new File("dataset2\\dataset2\\");
        File[] files = file.listFiles();
        MusicFile mp3;
        for(File f: files) {
            mp3 = new MusicFile(f);
            artistNameList.add(mp3.getArtistName());
        }
    }

    public ArtistName(String artistName,List<String> songNames){
        this.artistName = artistName;
        this.songNames = songNames;
    }


    public ArtistName(String name) {
        this.artistName = name;
        this.songNames = new ArrayList<>();
        File file = new File("dataset2\\dataset2\\");
        File[] files = file.listFiles();
        MusicFile mp3;
        for(File f: files){
            if(!f.getName().startsWith(".")) {
                mp3 = new MusicFile(f);
                if(mp3.isHasId3v2Tag()) {
                    if (mp3.getArtistName().equalsIgnoreCase(artistName) && mp3.getTrackName()!=null) {
                        songNames.add(mp3.getTrackName());
                    }
                }
            }
        }
    }

    public ArtistName(char start, char end) {
        File file = new File("dataset2\\dataset2\\");
        File[] files = file.listFiles();
        MusicFile mp3;
        artistNameList = new ArrayList<>();
        assert files != null;
        for(File f: files){
            if(!f.getName().startsWith(".")) {
                mp3 = new MusicFile(f);
                if(mp3.isHasId3v2Tag()) {
                    char startingChar = mp3.getArtistName().toUpperCase().charAt(0);
                    if (startingChar >= start && startingChar <= end && !artistNameList.contains(mp3.getArtistName())) {
                        artistNameList.add(mp3.getArtistName());
                    }
                }
            }
        }
    }

    public String getArtistName() {
        return artistName;
    }

    public List<String> getSongNames() {
        return songNames;
    }

    public List<String> getArtistNameList() { return artistNameList; }
}
