package logics.user_info;

import java.io.*;
import logics.model.UserProfile;

/*
  Loads and saves the single user profile to disk (data/profile.dat).
 */
public class UserDataBase {

    private static final String PROFILE_FILE_PATH = "data/profile.dat";

    public static void save(UserProfile profileToSave) {
        // ensure data/ directory exists
        new File("data").mkdirs();
        try (ObjectOutputStream outputStream =          //opens the file for writing "try" is used to automatically close the stream after writing, even if an error occurs
            new ObjectOutputStream(new FileOutputStream(PROFILE_FILE_PATH))) {
            outputStream.writeObject(profileToSave);    //converts the object into a byte stream and writes it to the file
        } catch (IOException saveException) {
            //if the disk is full, the program cant write to the folder
            //prints error in the console
                    }
    }

    /*
      Loads and returns the saved user profile.
      Returns null if no profile file exists or the file is corrupt.
     */
    public static UserProfile load() {
        File profileFile = new File(PROFILE_FILE_PATH);
        if (!profileFile.exists()) return null;

        try (ObjectInputStream inputStream =
                     new ObjectInputStream(new FileInputStream(profileFile))) {
            return (UserProfile) inputStream.readObject();
        } catch (Exception loadException) {
            return null;
        }
    }
}

