package wong.mp3.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;
import javax.media.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import wong.mp3.file.*;
import wong.mp3.player.*;

public class PlayerUI extends JFrame {
	/*
	 * ʵ������������ر��������Գ�����Լ���Ϊһ����
	 */
	private Player player;
	private Component mediaControl;
	private Container container;
	private URL fileUrl;

	/*
	 * ��������ر�����
	 */
	private JPanel infoPanel;
	private JPanel radioPanel;
	private JPanel buttonPanel;
	private JPanel songPanel;
	private JPanel songActionPanel;
	private JLabel songInfoLabel;
	private JRadioButton singleLoop;
	private JRadioButton allLoop;
	private JRadioButton randomLoop;
	private JRadioButton favoriteLoop;
	private ButtonGroup loopGroup;
	private JButton backwardButton;
	private JButton playButton;
	private JButton pauseButton;
	private JButton forwardButton;
	private JPanel searchSongPanel;
	private JLabel searchSongLabel;
	private PlayerJTextField selectSongNameField;
	protected JList songList;
	private DefaultListModel songListModel;
	private JButton addSongButton;
	private JButton removeSongButton;
	private JButton removeAllSongButton;

	// ö�ٱ���������˳��
	private enum PlayOrder {
		ORDER, SHUFFLE, REPEATONCE, MOST
	};

	private PlayOrder order=PlayOrder.ORDER;

	/*
	 * �����ļ���ر���
	 */
	private File song = null;
	private Time playTime;
	private int songNumber = 0;
	protected java.util.List<File> playList;
	private Mp3PlayList playListObject;
	private Mp3FilePersistent playListHandler;
	private java.util.Map<File, Integer> favoriteSongList;
	private FavoriteSongList favoriteSongListObject;
	private Mp3FilePersistent favoriteSongListHandler;
	private Mp3FileChooser filechooser;
	private float DEFAULT_VOLUME = 0.2f;
	private float volume;
	private String keyWord = "volume";

	/*
	 * ��������ʼ������
	 */
	Toolkit tk = Toolkit.getDefaultToolkit();
	Dimension screenSize = tk.getScreenSize();
	// �û����ã�����������������С���ã���û��Ҫд���־��ļ�
	// ����Java����Ҳ�ṩ��һ��Preferences�����ṩ��һЩ�û����õ�
	// ���书�ܡ�
	private Preferences preferences;
	private final int screenHeight = (int) (screenSize.height * 0.8);
	private final int screenWidth = (int) (screenSize.width * 0.3);

	public PlayerUI() {
		super("JFoobar");
		this.setSize(screenWidth, screenHeight);
		this.setResizable(false);

		initialize();
		preferences = Preferences.userNodeForPackage(this.getClass());
		volume = preferences.getFloat(keyWord, DEFAULT_VOLUME);

		container = getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		infoPanel = new JPanel();
		infoPanel.setPreferredSize(new Dimension(300, 50));
		infoPanel.setLayout(new GridLayout(2, 2));
		container.add(infoPanel);
		songInfoLabel = new JLabel();
		infoPanel.add(songInfoLabel);
		infoPanel.setBorder(BorderFactory.createEtchedBorder());

		radioPanel = new JPanel();
		radioPanel.setPreferredSize(new Dimension(300, 25));
		container.add(radioPanel);
		singleLoop = new JRadioButton("����ѭ��");
		singleLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (order == PlayOrder.MOST) {
					reOrderSongList();
				}
				order = PlayOrder.REPEATONCE;
			}
		});
		randomLoop = new JRadioButton("�������");
		randomLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (order == PlayOrder.MOST) {
					reOrderSongList();
				}
				order = PlayOrder.SHUFFLE;
			}
		});
		allLoop = new JRadioButton("˳�򲥷�", true);
		allLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (order == PlayOrder.MOST) {
					reOrderSongList();
				}
				order = PlayOrder.ORDER;
			}
		});
		favoriteLoop = new JRadioButton("�����");
		favoriteLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				order = PlayOrder.MOST;
				songListModel.removeAllElements();
				java.util.List<Map.Entry<File, Integer>> mostPlayList = favoriteSongListObject
						.getSortedList();
				for (Map.Entry<File, Integer> entry : mostPlayList) {
					File song = entry.getKey();
					songListModel.addElement(song.getName());
				}
				songList.setModel(songListModel);
			}
		});
		loopGroup = new ButtonGroup();
		loopGroup.add(allLoop);
		loopGroup.add(singleLoop);
		loopGroup.add(randomLoop);
		loopGroup.add(favoriteLoop);
		radioPanel.add(allLoop);
		radioPanel.add(singleLoop);
		radioPanel.add(randomLoop);
		radioPanel.add(favoriteLoop);

		/*
		 * ���������ư�ť��
		 */
		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(300, 50));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		container.add(buttonPanel);
		// ��һ�װ�ť
		backwardButton = new JButton("��һ��");
		// ����¼�����
		backwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				player.close();
				// getPlayerCompontents();
				removePlayerCompontents();
				if (order == PlayOrder.SHUFFLE) {
					songNumber = (int) ((Math.random() * 1000) % (playList
							.size()));
				} else {
					if (songNumber == 0) {
						songNumber = playList.size() - 1;
					} else {
						songNumber--;
					}
				}

				realizedPlayer();
				player.start();
			}
		});
		buttonPanel.add(backwardButton);

		// ���Ű�ť
		playButton = new JButton("����");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (player == null) {
					realizedPlayer();
					player.start();
				} else {
					player.setMediaTime(playTime);
					player.start();
				}
			}
		});
		buttonPanel.add(playButton);

		// ��ͣ��ť
		pauseButton = new JButton("��ͣ");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				playTime = player.getMediaTime();
				player.stop();
			}
		});
		buttonPanel.add(pauseButton);

		// ��һ�װ�ť��
		forwardButton = new JButton("��һ��");
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				player.close();
				// getPlayerCompontents();
				removePlayerCompontents();

				if (order == PlayOrder.SHUFFLE) {
					songNumber = (int) ((Math.random() * 1000) % (playList
							.size()));
				} else {
					if (songNumber >= playList.size() - 1) {
						songNumber = 0;
					} else {
						songNumber++;
					}
				}

				realizedPlayer();
				player.start();
			}
		});
		buttonPanel.add(forwardButton);

		/*
		 * �����б���塣
		 */
		songPanel = new JPanel();
		songPanel.setLayout(new BorderLayout());
		songPanel.setPreferredSize(new Dimension(300, 300));
		container.add(songPanel);
		Border border = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(border, "�����б�");
		songPanel.setBorder(titled);
		songList = new JList();
		songList.setBackground(Color.WHITE);
		songList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				// ����¼�����ѡ�
				songNumber = songList.getSelectedIndex();
			}
		});
		songList.addMouseListener(new MouseAdapter() {
			// �������¼�������˫��ʱ���š�
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					if (player != null) {
						player.close();
						removePlayerCompontents();
					}
					realizedPlayer();
					player.start();
				}
			}
		});
		songListModel = new DefaultListModel();
		songList.setModel(songListModel);
		JScrollPane songListScroll = new JScrollPane(songList);
		songPanel.add(songListScroll, BorderLayout.CENTER);

		searchSongPanel = new JPanel();
		searchSongPanel.setPreferredSize(new Dimension(300, 40));
		container.add(searchSongPanel);
		searchSongLabel = new JLabel("����");
		selectSongNameField = new PlayerJTextField(32);
		selectSongNameField.setSongList(songList);
		selectSongNameField.setPlayList(playList);
		searchSongPanel.add(searchSongLabel);
		searchSongPanel.add(selectSongNameField);

		/*
		 * �����б��¼�����塣
		 */
		songActionPanel = new JPanel();
		songActionPanel.setPreferredSize(new Dimension(300, 50));
		container.add(songActionPanel);

		removeAllSongButton = new JButton("�Ƴ��б�");
		removeAllSongButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int result = JOptionPane.showConfirmDialog(null, "ȷ��ɾ��ȫ�������ļ�?",
						"ɾ��", JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION && playList.size() != 0) {
					playList.clear();
					favoriteSongList.clear();
					songListModel.removeAllElements();
					playListObject.removeAllSongs();
					favoriteSongListObject.clear();
					playListHandler.writeObject(playListObject);
					favoriteSongListHandler.writeObject(favoriteSongListObject);
				}
			}
		});
		songActionPanel.add(removeAllSongButton);

		removeSongButton = new JButton("�Ƴ�����");
		removeSongButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (playList.size() != 0) {
					File song = playList.remove(songNumber);
					reOrderSongList();
					playListObject.setPlayList(playList);
					favoriteSongListObject.removeSong(song);
					playListHandler.writeObject(playListObject);
					favoriteSongListHandler.writeObject(favoriteSongListObject);
					updateSearchSongList();
				}
			}
		});
		songActionPanel.add(removeSongButton);

		addSongButton = new JButton("��Ӹ���");
		songActionPanel.add(addSongButton);
		addSongButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (playList.size() == 0) {
					filechooser = new Mp3FileChooser();
				} else {
					File temListFile = playList.get(0);
					String dir = temListFile.getParent();
					filechooser = new Mp3FileChooser(dir);
				}
				// ��ȡ���׸�����
				String[] filePaths = filechooser.OpenFile();

				if (filePaths != null) {
					for (String filePath : filePaths) {
						File tmpFile = new File(filePath);
						songListModel.addElement(tmpFile.getName());
						songList.setModel(songListModel);
						playList.add(tmpFile);
						favoriteSongListObject.addSong(tmpFile);
					}
					playListObject.setPlayList(playList);
					playListHandler.writeObject(playListObject);
					favoriteSongListHandler.writeObject(favoriteSongListObject);
				}
				updateSearchSongList();
			}
		});

		if (!playList.isEmpty()) {
			for (int i = 0; i < playList.size(); i++) {
				File tempFile = playList.get(i);
				songListModel.addElement(tempFile.getName());
			}
			songList.setModel(songListModel);
			updateSearchSongList();
			realizedPlayer();
			player.start();
		}
		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, Boolean.TRUE);
	}

	private void initialize() {
		playListHandler = Mp3FilePersistent.create("playlist.dat");
		favoriteSongListHandler = Mp3FilePersistent.create("favorite.dat");

		playListObject = (Mp3PlayList) playListHandler.readObject();
		if (playListObject == null) {
			playListObject = new Mp3PlayList();
		}
		playList = playListObject.getPlayList();

		favoriteSongListObject = (FavoriteSongList) favoriteSongListHandler
				.readObject();
		if (favoriteSongListObject == null) {
			favoriteSongListObject = new FavoriteSongList();
		}
		favoriteSongList = favoriteSongListObject.getFavoriteSongList();

	}

	private void reOrderSongList() {
		songListModel.removeAllElements();
		for (int i = 0; i < playList.size(); i++) {
			File tempFile = playList.get(i);
			songListModel.addElement(tempFile.getName());
		}
		songList.setModel(songListModel);
	}

	/*
	 * 
	 */
	private void updateSearchSongList() {
		selectSongNameField.clearCompletion();
		int size = playList.size();
		for (int i = 0; i < size; i++) {
			File song = playList.get(i);
			selectSongNameField.addCompletion(song.getName());
		}
	}

	/*
	 * ��ʾ������Ϣ��
	 */
	public void showErrorMessage(String error) {
		JOptionPane.showMessageDialog(this, error, "error",
				JOptionPane.ERROR_MESSAGE);
	}

	// �Ƴ����ƿؼ���
	public void removePlayerCompontents() {
		if (mediaControl != null) {
			// ������֮ǰ�Ȼ�ȡ��ǰ������ֵ��
			volume = player.getGainControl().getLevel();
			preferences.putFloat(keyWord, volume);
			infoPanel.remove(mediaControl);
		}

		player.close();
	}

	public void setPlayerCompontents() {
		mediaControl = player.getControlPanelComponent();
		if (mediaControl != null) {
			// ���ؼ���ӵ�����С�
			// ����������
			player.getGainControl().setLevel(volume);
			Component add = infoPanel.add(mediaControl);
		}
	}

	/*
	 * ���ļ�ʵ������������
	 */
	public void realizedPlayer() {
		song = playList.get(songNumber);
		favoriteSongListObject.update(song);
		favoriteSongListHandler.writeObject(favoriteSongListObject);
		if (song != null) {
			try {
				fileUrl = song.toURL();
				super.setTitle(song.getName());// /////////////////////
				songInfoLabel.setText(song.getName());
				songInfoLabel.setHorizontalAlignment(JLabel.CENTER);
				songInfoLabel.setVerticalAlignment(JLabel.CENTER);
				Font font = new Font("", Font.BOLD, 15);
				songInfoLabel.setFont(font);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				showErrorMessage("Bad URL");
			}
			makePlayer(fileUrl.toString());
		}
	}

	public void makePlayer(String mediaLocation) {
		if (player != null) {
			removePlayerCompontents();
		}
		MediaLocator mediaLocator = new MediaLocator(mediaLocation);
		if (mediaLocator == null) {
			showErrorMessage("Error open file");
			return;
		}
		try {
			player = Manager.createPlayer(mediaLocator);
			player.addControllerListener(new PlayerEventHandler());
			player.realize();
		} catch (NoPlayerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class PlayerEventHandler extends ControllerAdapter {

		@Override
		public void realizeComplete(RealizeCompleteEvent evt) {
			player.prefetch();
		}

		@Override
		public void prefetchComplete(PrefetchCompleteEvent evt) {

			setPlayerCompontents();
			validate();
			player.start();

		}

		@Override
		public void endOfMedia(EndOfMediaEvent evt) {
			// getPlayerCompontents();
			if (order == PlayOrder.ORDER) {
				player.close();
				removePlayerCompontents();
				if (songNumber >= playList.size()) {
					songNumber = 0;
				} else {
					songNumber++;
				}
				realizedPlayer();
				player.start();
			} else if (order == PlayOrder.REPEATONCE) {
				player.setMediaTime(new Time(0));
				player.start();
			} else {
				player.close();
				removePlayerCompontents();
				songNumber = (int) ((Math.random() * 1000) % (playList.size()));
				realizedPlayer();
				player.start();
			}
		}
	}
}
