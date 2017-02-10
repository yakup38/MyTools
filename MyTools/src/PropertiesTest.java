import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesTest {
    public static void main(String[] args)
        throws Exception {

    	File f = new File("/Test.xml");
    	String xml = new String (Files.readAllBytes(Paths.get(f.getPath())), "UTF-8");
    	
    	String value = getNodeValueByName("Number", xml);
    	
        System.out.println("value = " + value);
    }
    
    
	private static String getNodeValueByName(String node, String xml) {
		String value = null;
		String patternString = ".*?<" + node + ">(.*?)</"+ node + ">.*?";
			Pattern pattern = Pattern.compile(patternString , Pattern.DOTALL);
				Matcher matcher = pattern.matcher(xml);
				boolean result = matcher.matches();
				if (result) {
					value = matcher.group(1);
					if (value != null) {
						value = value.trim();
					}
				}
		return value;
}
    
    
}