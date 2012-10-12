package viper.ui;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.*;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

public class ExampleOne {

  public static List<SongEntry> allMySongs = Arrays.asList(
        new SongEntry(1, "Battery",                   "Metallica",                "Master of Puppets",    "Elektra Records",      "Hard Rock"),
        new SongEntry(5, "Fixing a Hole",             "The Beatles",              "Sgt. Pepper's Lonely Hearts Club Band", "Parlophone", "Pop Rock"),
        new SongEntry(1, "Enter Sandman",             "Metallica",                "Metallica",            "Elektra Records",      "Hard Rock"),
        new SongEntry(2, "Master of Puppets",         "Metallica",                "Master of Puppets",    "Elektra Records",      "Hard Rock"),
        new SongEntry(4, "Stairway to Heaven",        "Led Zeppelin",             "IV",                   "Atlantic",             "Hard Rock"),
        new SongEntry(3, "Disposable Heroes",         "Metallica",                "Master of Puppets",    "Elektra Records",      "Hard Rock"),
        new SongEntry(8, "Nothing Else Matters",      "Metallica",                "Metallica",            "Elektra Records",      "Hard Rock"),
        new SongEntry(3, "Lucy in the Sky with Diamonds", "The Beatles",          "Sgt. Pepper's Lonely Hearts Club Band", "Parlophone", "Pop Rock"),
        new SongEntry(1, "Frank Sinatra",             "Cake",                     "Fashion Nugget",       "Volcano",              "Folk Rock"),
        new SongEntry(2, "The Distance",              "Cake",                     "Fashion Nugget",       "Volcano",              "Folk Rock"),
        new SongEntry(3, "Friend is a Four-letter word","Cake",                   "Fashion Nugget",       "Volcano",              "Folk Rock"),
        new SongEntry(1, "Opera Singer",              "Cake",                     "Comfort Eagle",        "Sony",                 "Folk Rock"),
        new SongEntry(2, "Meanwhile, Rick James...",  "Cake",                     "Comfort Eagle",        "Sony",                 "Folk Rock"),
        new SongEntry(4, "Short Skirt Long Jacket",   "Cake",                     "Comfort Eagle",        "Sony",                 "Folk Rock"),
        new SongEntry(7, "Sheep Go To Heaven",        "Cake",                     "Prolonging The Magic", "Capricorn",            "Folk Rock"),
        new SongEntry(1, "Lose Yourself",             "Eminem",                   "8-Mile Soundtrack",    "Umvd Import",          "Rap"),
        new SongEntry(2, "Shoot To Thrill",           "ACDC",                     "Back in Black",        "Atlantic / WEA",       "Hard Rock"),
        new SongEntry(9, "High Voltage",              "ACDC",                     "High Voltage",         "Atlantic / WEA",       "Hard Rock"),
        new SongEntry(1, "Thunderstruck",             "ACDC",                     "The Razor's Edge",     "Sony",                 "Hard Rock"),
        new SongEntry(1, "Smells Like Teen Spirit",   "Nirvana",                  "Nevermind",            "Geffen",               "Alternative Rock"),
        new SongEntry(2, "In Bloom",                  "Nirvana",                  "Nevermind",            "Geffen",               "Alternative Rock"),
        new SongEntry(3, "Come As You Are",           "Nirvana",                  "Nevermind",            "Geffen",               "Alternative Rock"),
        new SongEntry(4, "Breed",                     "Nirvana",                  "Nevermind",            "Geffen",               "Alternative Rock"),
        new SongEntry(5, "Lithium",                   "Nirvana",                  "Nevermind",            "Geffen",               "Alternative Rock"),
        new SongEntry(8, "Drain You",                 "Nirvana",                  "Nevermind",            "Geffen",               "Alternative Rock"),
        new SongEntry(1, "Serve the Servants",        "Nirvana",                  "In Utero",             "Geffen",               "Alternative Rock"),
        new SongEntry(2, "Scentless Apprentice",      "Nirvana",                  "In Utero",             "Geffen",               "Alternative Rock"),
        new SongEntry(3, "Heart-Shaped Box",          "Nirvana",                  "In Utero",             "Geffen",               "Alternative Rock"),
        new SongEntry(4, "Rape Me",                   "Nirvana",                  "In Utero",             "Geffen",               "Alternative Rock"),
        new SongEntry(12,"All Apologies",             "Nirvana",                  "In Utero",             "Geffen",               "Alternative Rock"),
        new SongEntry(1, "Burnout",                   "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(2, "Having a Blast",            "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(3, "Chump",                     "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(4, "Longview",                  "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(5, "Welcome to Paradise",       "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(6, "Pulling Teeth",             "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(7, "Basket Case",               "Green Day",                "Dookie",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(1, "Nice Guys Finish Last",     "Green Day",                "Nimrod",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(2, "Hitchin' a Ride",           "Green Day",                "Nimrod",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(3, "The Grouch",                "Green Day",                "Nimrod",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(4, "Redundant",                 "Green Day",                "Nimrod",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(8, "Platypus (I Hate You)",     "Green Day",                "Nimrod",               "Reprise / WEA",        "Punk Rock"),
        new SongEntry(1, "Paint It Black",            "Rolling Stones",           "Aftermath",            "Abkco",                "Hard Rock")
  );

  /**
   * An artist, album or track.
   */
  public static class SongEntry {
    private final String artist, album, trackName, recordCompany, genre;
    private final Integer trackNumber;

    public SongEntry(Integer trackNumber, String trackName, String artist, String album, String recordCompany, String genre) {
      this.artist = artist;
      this.album = album;
      this.trackName = trackName;
      this.trackNumber = trackNumber;
      this.recordCompany = recordCompany;
      this.genre = genre;
    }

    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getTrackName() { return trackName; }
    public Integer getTrackNumber() { return trackNumber; }
    public String getRecordCompany() { return recordCompany; }
    public String getGenre() { return genre; }
    public String getName() {
      if (trackName != null) return trackName;
      if (album != null) return album;
      if (artist != null) return artist;
      if (genre != null) return genre;
      if (recordCompany != null) return recordCompany;

      throw new IllegalStateException();
    }
    public SongEntry toAlbum() {
      return new SongEntry(null, null, null, album, null, null);
    }
    public SongEntry toArtist() {
      return new SongEntry(null, null, artist, null, null, null);
    }
    public SongEntry toGenre() {
      return new SongEntry(null, null, null, null, null, genre);
    }
    public SongEntry toRecordCompany() {
      return new SongEntry(null, null, null, null, recordCompany, null);
    }
    public String toString() {
      return getName();
    }
    public int hashCode() {
      return getName().hashCode();
    }
    public boolean equals(Object obj) {
      return getName().equals(((SongEntry)obj).getName());
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        BasicEventList<SongEntry> songsEventList = new BasicEventList<SongEntry>();
        songsEventList.addAll(allMySongs);

        SortedList<SongEntry> sortedSongs = new SortedList<SongEntry>(songsEventList, null);

        JTextField filterField = new JTextField(20);

        // combine the filter field in a a nice label in a panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterField);

        TextFilterator songsTextFilterator = GlazedLists.textFilterator(SongEntry.class, "album", "name", "trackNumber", "recordCompany", "artist");
        TextComponentMatcherEditor textMatcherEditor = new TextComponentMatcherEditor<SongEntry>(filterField, songsTextFilterator);
        FilterList<SongEntry> filteredSongs = new FilterList<SongEntry>(sortedSongs, textMatcherEditor);


        FunctionList.Function<SongEntry,String> songToArtist = new FunctionList.Function<SongEntry,String>() {
          public String evaluate(SongEntry sourceValue) {
            return sourceValue.getArtist();
          }
        };
        EventList<String> artists = new FunctionList<SongEntry,String>(sortedSongs, songToArtist);
        EventList<String> uniqueArtists = new UniqueList<String>(artists);
        JList artistsJList = new JList(new EventListModel<String>(uniqueArtists));

        TreeList.Format<SongEntry> treeFormat = new SongTreeFormat();
        TreeList<SongEntry> songsTree = new TreeList<SongEntry>(filteredSongs, treeFormat, TreeList.NODES_START_EXPANDED);


        // create a JTable to display the songs
        final TableFormat<SongEntry> tableFormat = new SongTableFormat();
        final EventTableModel<SongEntry> tableModel =
              new EventTableModel<SongEntry>(songsTree, tableFormat);
        final JTable songTable = new JTable(tableModel);

        TableComparatorChooser.install(songTable, sortedSongs,
              TableComparatorChooser.SINGLE_COLUMN);
        TreeTableSupport.install(songTable, songsTree, 1);

        // show everything in a table
        JFrame frame = new JFrame("Song Browser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().add(filterPanel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(songTable), BorderLayout.CENTER);
        frame.getContentPane().add(new JScrollPane(artistsJList), BorderLayout.WEST);
        frame.setVisible(true);
      }
    });
  }

  public static class SongTableFormat implements TableFormat<SongEntry> {

    public int getColumnCount() {
      return 5;
    }

    public String getColumnName(int column) {
      switch(column) {
        case 0: return "Track Number";
        case 1: return "Name";
        case 2: return "Album";
        case 3: return "Artist";
        case 4: return "Record Company";
      }
      throw new IllegalArgumentException();
    }

    public Object getColumnValue(SongEntry baseObject, int column) {
      switch(column) {
        case 0: return baseObject.getTrackNumber();
        case 1: return baseObject.getName();
        case 2: return baseObject.getAlbum();
        case 3: return baseObject.getArtist();
        case 4: return baseObject.getRecordCompany();
      }
      throw new IllegalArgumentException();
    }
  }

  public static class SongTreeFormat implements TreeList.Format<SongEntry> {

    public void getPath(List<SongEntry> path, SongEntry element) {
      path.add(element.toRecordCompany());
      path.add(element.toArtist());
      path.add(element.toAlbum());
      path.add(element);
    }

    public boolean allowsChildren(SongEntry element) {
      return true;
    }

    public Comparator<SongEntry> getComparator(int depth) {
      switch(depth) {
        case 0 : return GlazedLists.beanPropertyComparator(SongEntry.class, "recordCompany");
        case 1 : return GlazedLists.beanPropertyComparator(SongEntry.class, "artist");
        case 2 : return GlazedLists.beanPropertyComparator(SongEntry.class, "album");
      }
      return null;
    }
  }
}