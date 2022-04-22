import javax.swing.*;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class ClientLauncher {
	private static final String GAME_TITLE = "Shanty";

	private static final Map<String, String> PARAMETERS = new HashMap<>();

	static {
		PARAMETERS.put("1", "1");
		PARAMETERS.put("2", "https://payments.jagex.com/");
		PARAMETERS.put("3", "true");
		PARAMETERS.put("4", "1");
		PARAMETERS.put("5", "1");
		PARAMETERS.put("6", "0");
		PARAMETERS.put("7", "0");
		PARAMETERS.put("8", "true");
		PARAMETERS.put("9", "ElZAIrq5NpKN6D3mDdihco3oPeYN2KFy2DCquj7JMmECPmLrDP3Bnw");
		PARAMETERS.put("10", "5");
		PARAMETERS.put("11", "https://auth.jagex.com");
		PARAMETERS.put("12", "301");
		PARAMETERS.put("13", ".runescape.com");
		PARAMETERS.put("14", "0");
		PARAMETERS.put("15", "0");
		PARAMETERS.put("16", "false");
		PARAMETERS.put("17", "http://www.runescape.com/g=oldscape/slr.ws?order=LPWM");
		PARAMETERS.put("18", "");
		PARAMETERS.put("19", "196515767263-1oo20deqm6edn7ujlihl6rpadk9drhva.apps.googleusercontent.com");
		PARAMETERS.put("20", "https://token-auth.production.jxp.aws.jagex.com/");
		PARAMETERS.put("21", "0");
		PARAMETERS.put("22", "0");
	}

	public static void main(String[] args) {
		AppletStub appletStub = new ClientStub();
		Client client = new Client();

		JFrame frame = new JFrame(GAME_TITLE);

		frame.add(client);
		frame.setResizable(true);
		frame.setVisible(true);
		frame.setSize(781, 541);
		frame.setLocation(40, 40);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.toFront();

		client.setStub(appletStub);
		client.init();
		client.start();
	}

	private static class ClientStub implements AppletStub {
		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public URL getDocumentBase() {
			return getCodeBase();
		}

		@Override
		public URL getCodeBase() {
			String urlString = "http://oldschool1.runescape.com/";

			URL url;
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Given URL of '" + urlString + "' turned out to be invalid", e);
			}

			return url;
		}

		@Override
		public String getParameter(String s) {
			return PARAMETERS.get(s);
		}

		@Override
		public AppletContext getAppletContext() {
			return null;
		}

		@Override
		public void appletResize(int width, int height) {
			// nothing
		}
	}
}
