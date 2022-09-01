
/*
*   @version 1.2 2012-06-29
*   @author wangzheng
*/

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
*�ڷ�������������£������ͻ��ˣ������׽��ֽ���ͼ��
*/

public class ImageCtrlServer {	
    public static ServerSocket ss = null;
    
    public static void main(String args[]) throws Exception,IOException{    
    	ss = new ServerSocket(6000);
        
        final ImageFrame frame = new ImageFrame(ss);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
       
        while(true){
        	frame.panel.getimage();
            frame.repaint();
        }        
    }
       
}

/** 
    A frame with an image panel
*/
@SuppressWarnings("serial")
class ImageFrame extends JFrame{
	public ImagePanel panel;
	public JButton jb;
   
    public ImageFrame(ServerSocket ss)throws Exception{
   	    // get screen dimensions   	   
   	    Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        String IPname = null;
        
        try
        {
        	System.out.println("������IP = " + InetAddress.getLocalHost().getHostAddress()+" �������װ��VMware�����IP��ַΪ������ĵ�ַ");
        	IPname = InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e){ 
        	e.printStackTrace();
        }
        // center frame in screen
        setTitle("ImageTest"+"������IP = " + IPname);
        setLocation((screenWidth - DEFAULT_WIDTH) / 2, (screenHeight - DEFAULT_HEIGHT) / 2);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // add panel to frame
        this.getContentPane().setLayout(null);
        panel = new ImagePanel(ss);
        panel.setSize(640,480);
		//panel.setSize(1280,720);
		//panel.setSize(1920,1080);
		//panel.setSize(1440,720);
        panel.setLocation(0, 0);
        add(panel);
        jb = new JButton("���������");
        jb.setBounds(0,480,640,50);
        add(jb);
        
        jb.addKeyListener(new KeyAdapter() {
        	ServerSocket ss;
        	boolean sendFlag = false;//���ñ�־λ,����ʱִֻ��һ��,����������
        	public void keyPressed(KeyEvent e) {
        		int KeyCode = e.getKeyCode(); // ������������Ӧ������ֵ
        		String s = KeyEvent.getKeyText(KeyCode); // ���ذ������ַ�������
        		System.out.print("���������Ϊ��" + s + ",");
        		System.out.println("��Ӧ��KeyCodeΪ��" + KeyCode);
        		if(!sendFlag) {
        		try{
        			ss = new ServerSocket(7788);
        			send(KeyCode);
        			ss.close();
        			sendFlag=true;
        		}catch (Exception e1) {

        			e1.printStackTrace();
        		}
        		}
        		
        	}
        	public void keyReleased(KeyEvent e) {
        		int KeyCode = e.getKeyCode(); // ������������Ӧ������ֵ
        		if(KeyCode==87||KeyCode==83||KeyCode==65||KeyCode==68||KeyCode==81||KeyCode==69) {
        			try {
        				ss = new ServerSocket(7788);
						stop();
						sendFlag=false;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
        		}
        	}

			public void send(int i) throws Exception{
					@SuppressWarnings("resource")
					ServerSocket serverSocket = ss;//new ServerSocket(7788); // ����ServerSocket����
					Socket client = serverSocket.accept(); // ����ServerSocket��accept()������������
					OutputStream os = client.getOutputStream();// ��ȡ�ͻ��˵������
					System.out.println("��ʼ��ͻ��˽�������");
					switch (i) {
	        		case 87:os.write(("01").getBytes());break;//w��
	        		case 83:os.write(("02").getBytes());break;//s��
	        		case 65:os.write(("03").getBytes());break;//a��
	        		case 68:os.write(("04").getBytes());break;//d��
					case 81:os.write(("13").getBytes());break;//q����ת
					case 69:os.write(("14").getBytes());break;//e����ת
					case 49:os.write(("s1").getBytes());break;//�ٶ�1
					case 50:os.write(("s2").getBytes());break;//�ٶ�2
					case 51:os.write(("s3").getBytes());break;//�ٶ�3
					case 52:os.write(("s").getBytes());break;//ȫ��
					case 16:os.write(("L").getBytes());break;//shift�����
					case 67:os.write(("C").getBytes());break;//c�л�����ͷ
	        		} 			
					
					System.out.println("������ͻ��˽�������");
					os.close();
					client.close();
			}
			protected void stop() throws Exception {
				ServerSocket serverSocket = ss;// ����ServerSocket����
				Socket client = serverSocket.accept(); // ����ServerSocket��accept()������������
				OutputStream os = client.getOutputStream();// ��ȡ�ͻ��˵������
				os.write(("05").getBytes());//ֹͣ
				os.close();
				client.close();
				ss.close();
			}
        });
    }


	public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT = 560;  
}

/**
   A panel that displays a tiled image
*/
@SuppressWarnings("serial")
class ImagePanel extends JPanel {     
    private ServerSocket ss;
    private Image image;
    private InputStream ins;
	 
    public ImagePanel(ServerSocket ss) {  
	    this.ss = ss;
    }
    
    public void getimage() throws IOException{
    	Socket s = this.ss.accept();
        System.out.println("���ӳɹ�!");
        this.ins = s.getInputStream();
		this.image = ImageIO.read(ins);
		this.ins.close();
    }
   
    public void paintComponent(Graphics g){  
        super.paintComponent(g);    
        if (image == null) return;
        g.drawImage(image, 0, 0,640,480, null);
		//Ҫ��ʾ��ͼƬ����ˮƽλ�ã���ֱλ�ã�ͼƬ���¿�ȣ��¸߶ȣ�Ҫ֪ͨ��ͼ��۲���
    }

}