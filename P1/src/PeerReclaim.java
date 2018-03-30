import java.io.File;

public class PeerReclaim extends Peer implements Runnable{
    int maxSize;
    public PeerReclaim(int file_name) {
        this.maxSize = file_name;
    }

    @Override
    public void run() {
        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();
        int i;
        int sizeAccumulator = 0;

        //iterate through files with size smaller than the available size
        for (i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Chunk")) {
                System.out.println("File " + listOfFiles[i].getName() + "has "+ listOfFiles[i].length() + "bytes" );
                sizeAccumulator += listOfFiles[i].length();
            }
            if(sizeAccumulator > this.maxSize){
                i--;
                break;
            }
        }
        System.out.println("total size" + sizeAccumulator);
    }
}
