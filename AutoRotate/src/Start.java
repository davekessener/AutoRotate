import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Start extends Application
{
	private static String sArgument;
	
	private Stage mPrimary;
	private BorderPane mRoot;
	private TextArea mOutput;
	private Label mInfo;
	private ProgressBar mProgress;
	private Button mToggle;
	private List<String> mFiles;
	private int mCurrent, mTotal;
	
	public Start()
	{
		super();
		
		mRoot = new BorderPane();
		mOutput = new TextArea();
		mProgress = new ProgressBar();
		mToggle = new Button("V");
		mInfo = new Label("");
		
		mOutput.setEditable(false);
		
		mProgress.setPrefWidth(Double.MAX_VALUE);
		mProgress.setPrefHeight(32);
		
		mToggle.setOnAction(e -> toggle());
	}
	
	public static void main(String[] args)
	{
		sArgument = args[0];
		if(!sArgument.endsWith(File.separator)) sArgument += File.separator;
		launch(new String[] {});
	}

	@Override
	public void start(Stage primary) throws Exception
	{
		Scene scene = new Scene(mRoot, 400, 50);
		
		mPrimary = primary;
		
		mFiles = getAllFiles(JPG_PTRN);
		mCurrent = 0;
		mTotal = mFiles.size();
		
		AnchorPane ap = new AnchorPane();
		ap.getChildren().addAll(mInfo, mToggle);
		AnchorPane.setLeftAnchor(mInfo, 5D);
		AnchorPane.setTopAnchor(mInfo, 7D);
		AnchorPane.setRightAnchor(mToggle, 5D);
		
		mRoot.setTop(ap);
		mRoot.setCenter(mProgress);
		
		primary.setTitle("AutoRotate");
		primary.setScene(scene);
		primary.setResizable(false);
		primary.show();
		
		process();
	}
	
	private void toggle()
	{
		mRoot.setCenter(mOutput);
		mRoot.setTop(mProgress);
		mPrimary.setResizable(true);
		mPrimary.setWidth(800D);
		mPrimary.setHeight(300D);
		mPrimary.centerOnScreen();
	}
	
	private void process()
	{
		if(!mFiles.isEmpty())
		{
			String f = mFiles.remove(0);
			
			info(f.substring(sArgument.length()));
			mProgress.setProgress((++mCurrent) / (double) mTotal);
			
			try
			{
				Process process = (new ProcessBuilder("C:\\Program Files\\AutoRotate\\nconvert.exe", "-jpegtrans", "exif", "-overwrite", f)).start();
				
				(new Thread(() -> {
					try
					{
						process.waitFor();
						
						Platform.runLater(() -> process());
					}
					catch (InterruptedException e)
					{
						Platform.runLater(() -> {
							log("Error:\n" + e.getLocalizedMessage());
							
							process();
						});
					}
				})).start();
			}
			catch (IOException e)
			{
				log("Error:\n" + e.getLocalizedMessage());
				
				process();
			}
		}
		else
		{
			mInfo.setText("DONE");
			log("DONE");
		}
	}
	
	private void info(String s)
	{
		log(s);
		mInfo.setText("Converting: " + s);
	}
	
	private void log(String s)
	{
		mOutput.appendText(s + "\n");
	}
	
	private static List<String> getAllFiles(Pattern ptrn)
	{
		List<String> a = new ArrayList<>();
		
		getAllFiles(new File(sArgument), a, ptrn);
		
		return a;
	}
	
	private static void getAllFiles(File dir, List<String> a, Pattern ptrn)
	{
		for(File f : dir.listFiles())
		{
			if(f.isDirectory())
			{
				getAllFiles(f, a, ptrn);
			}
			else if(ptrn.matcher(f.getAbsolutePath()).find())
			{
				a.add(f.getAbsolutePath());
			}
		}
	}
	
	private static final Pattern JPG_PTRN = Pattern.compile("\\.jpe?g$");
}
