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
	 * 实例化播放器相关变量，可以抽出来自己作为一个类
	 */
	private Player player;
	private Component mediaControl;
	private Container container;
	private URL fileUrl;

	/*
	 * 播放器相关变量。
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

	// 枚举变量，播放顺序
	private enum PlayOrder {
		ORDER, SHUFFLE, REPEATONCE, MOST
	};

	private PlayOrder order=PlayOrder.ORDER;

	/*
	 * 歌曲文件相关变量
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
	 * 播放器初始化设置
	 */
	Toolkit tk = Toolkit.getDefaultToolkit();
	Dimension screenSize = tk.getScreenSize();
	// 用户配置，像音量设置这样的小设置，就没必要写进持久文件
	// 而且Java本身也提供了一个Preferences类来提供给一些用户设置的
	// 记忆功能。
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
		singleLoop = new JRadioButton("单曲循环");
		singleLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (order == PlayOrder.MOST) {
					reOrderSongList();
				}
				order = PlayOrder.REPEATONCE;
			}
		});
		randomLoop = new JRadioButton("随机播放");
		randomLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (order == PlayOrder.MOST) {
					reOrderSongList();
				}
				order = PlayOrder.SHUFFLE;
			}
		});
		allLoop = new JRadioButton("顺序播放", true);
		allLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (order == PlayOrder.MOST) {
					reOrderSongList();
				}
				order = PlayOrder.ORDER;
			}
		});
		favoriteLoop = new JRadioButton("最常播放");
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
		 * 播放器控制按钮板
		 */
		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(300, 50));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		container.add(buttonPanel);
		// 上一首按钮
		backwardButton = new JButton("上一首");
		// 添加事件处理
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

		// 播放按钮
		playButton = new JButton("播放");
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

		// 暂停按钮
		pauseButton = new JButton("暂停");
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				playTime = player.getMediaTime();
				player.stop();
			}
		});
		buttonPanel.add(pauseButton);

		// 下一首按钮。
		forwardButton = new JButton("下一首");
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
		 * 歌曲列表面板。
		 */
		songPanel = new JPanel();
		songPanel.setLayout(new BorderLayout());
		songPanel.setPreferredSize(new Dimension(300, 300));
		container.add(songPanel);
		Border border = BorderFactory.createEtchedBorder();
		Border titled = BorderFactory.createTitledBorder(border, "歌曲列表");
		songPanel.setBorder(titled);
		songList = new JList();
		songList.setBackground(Color.WHITE);
		songList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				// 鼠标事件处理。选项。
				songNumber = songList.getSelectedIndex();
			}
		});
		songList.addMouseListener(new MouseAdapter() {
			// 添加鼠标事件处理。当双击时播放。
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
		searchSongLabel = new JLabel("搜索");
		selectSongNameField = new PlayerJTextField(32);
		selectSongNameField.setSongList(songList);
		selectSongNameField.setPlayList(playList);
		searchSongPanel.add(searchSongLabel);
		searchSongPanel.add(selectSongNameField);

		/*
		 * 歌曲列表事件处面板。
		 */
		songActionPanel = new JPanel();
		songActionPanel.setPreferredSize(new Dimension(300, 50));
		container.add(songActionPanel);

		removeAllSongButton = new JButton("移除列表");
		removeAllSongButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int result = JOptionPane.showConfirmDialog(null, "确定删除全部歌曲文件?",
						"删除", JOptionPane.YES_NO_CANCEL_OPTION);
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

		removeSongButton = new JButton("移除歌曲");
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

		addSongButton = new JButton("添加歌曲");
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
				// 获取多首歌曲。
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
	 * 显示错误信息。
	 */
	public void showErrorMessage(String error) {
		JOptionPane.showMessageDialog(this, error, "error",
				JOptionPane.ERROR_MESSAGE);
	}

	// 移除控制控件。
	public void removePlayerCompontents() {
		if (mediaControl != null) {
			// 在销毁之前先获取当前的音量值。
			volume = player.getGainControl().getLevel();
			preferences.putFloat(keyWord, volume);
			infoPanel.remove(mediaControl);
		}

		player.close();
	}

	public void setPlayerCompontents() {
		mediaControl = player.getControlPanelComponent();
		if (mediaControl != null) {
			// 将控件添加到面板中。
			// 设置音量。
			player.getGainControl().setLevel(volume);
			Component add = infoPanel.add(mediaControl);
		}
	}

	/*
	 * 从文件实例化播放器。
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
