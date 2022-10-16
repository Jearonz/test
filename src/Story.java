import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Story {
    private LinkedList<String> story = new LinkedList<>();

    public void addStoryE1(String e1){
        // если сообщений больше 10, удаляем первое и добавляем новое
        // иначе просто добавить
        if (story.size() >= 10) {
            story.removeFirst();
            story.add(e1);
        }
        else {
            story.add(e1);
        }
    }

    public void printStory(BufferedWriter writer){
        if (story.size() > 0) {
            try{
                writer.write("History messages" + "\n");
                for (String vr: story) {
                    writer.write(vr + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            } catch (IOException ignored){}
        }
    }
}