import java.io.*;
import com.mpatric.mp3agic.*;

import java.nio.file.Files;
import java.util.Arrays;

public class MusicFile {

    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private ID3v2 tag;
    private byte[] musicFileExtract;
    private byte[][] chunkArray;
    private byte[] chunk;
    private boolean hasId3v2Tag;

    /**Copy Constructor*/
    public MusicFile(String t,int i) {

        File folder = new File("dataset2\\dataset2\\");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if(!file.getName().startsWith(".")) {
                try {
                    MusicFile mp3F = new MusicFile(file);
                    if(mp3F.getTrackName()!=null) {
                        if (mp3F.getTrackName().equalsIgnoreCase(t)) {
                            this.musicFileExtract = Files.readAllBytes(file.toPath());
                            this.chunkArray = divideArray(musicFileExtract, 512000);
                            this.chunk = chunkArray[i];
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error in chunks");
                }
            }
        }

    }
    /**Following method constructs a new music file object*/
    public MusicFile(File myFile) {
        try {

            Mp3File mp3File = new Mp3File(myFile);
            if (mp3File.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3File.getId3v2Tag();
                this.tag = id3v2Tag;
                this.artistName = id3v2Tag.getArtist();
                this.trackName = id3v2Tag.getTitle();
                this.albumInfo = id3v2Tag.getAlbum();
                this.genre = id3v2Tag.getGenreDescription();
                this.hasId3v2Tag = true;
            }
        } catch (IOException | InvalidDataException | UnsupportedTagException e) {
            e.printStackTrace();
        }
    }



    public static byte[][] divideArray(byte[] source, int chunksize) {


        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];

        int start = 0;

        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
            start += chunksize ;
        }

        return ret;
    }

    public ID3v2 getTag() {
        return tag;
    }

    public byte[] getSong(int i) {
        return chunkArray[i];
    }

    public byte[] getChunk() {
        return chunk;
    }

    public byte[][] getChunkArray() {
        return chunkArray;
    }

    public String getTrackName() {
        return trackName;
    }

    public boolean isHasId3v2Tag() {
        return hasId3v2Tag;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumInfo() {
        return albumInfo;
    }

    public String getGenre() {
        return genre;
    }

    public byte[] getMusicFileExtract() {
        return musicFileExtract;
    }



    @Override
    public String toString() {
        return "\nTrack Name: " + trackName +
                "\nArtist Name: " + artistName +
                "\nAlbum Info: " + albumInfo +
                "\nGenre: " + genre +"\n --------------------------";
    }
}