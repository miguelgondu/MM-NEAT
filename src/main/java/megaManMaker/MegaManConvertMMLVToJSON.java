package megaManMaker;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import gvgai.tools.IO;

public class MegaManConvertMMLVToJSON {
	public static int maxX = 0;
	public static int maxY = 0;
	public static void main(String[] args) {
		int i = 1;
		
		List<List<Integer>> level = convertMMLVtoInt(MegaManVGLCUtil.MEGAMAN_MMLV_PATH+"MegaManLevel"+i+".mmlv");
		MegaManVGLCUtil.printLevel(level);
	}
	
	public static List<List<Integer>> convertMMLVtoInt(String mmlvFile) {
		File mmlv = new File(mmlvFile);
//		String oldString = "Hello my name is kec";
//		String newString = oldString.replace("k", "d").trim();
//		System.out.println(newString);
		HashSet<Point> activatedScreen = new HashSet<>();
		Scanner scan = null;
		try {
			scan = new Scanner(mmlv);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//int k = scan.nextInt();
		List<List<Integer>> blockxyIDList = new ArrayList<>();
		while(scan.hasNext()) {
			String l = scan.next();
			//IntStream intStr = l.chars();
			
			if(!l.startsWith("2")&&!l.startsWith("1")&&!l.startsWith("4")&&!l.startsWith("0")
					&&!l.startsWith("o")&&!l.startsWith("b")&&!l.startsWith("a")
					&&!l.startsWith("e")&&!l.startsWith("f")&&!l.startsWith("g")&&!l.startsWith("h")&&
					!l.startsWith("j")&&!l.startsWith("k")&&!l.startsWith("l")&&!l.startsWith("m")&&
					!l.startsWith("n")&&!l.startsWith("[")
					) { //shows us all blocks (solid, spike, ladder), enemies, player
				if(l.startsWith("i")) {
					
					String k = l;
					k=k.replace("i", "i ");
					k=k.replace(",", " ");
					k=k.replace("\"", "");
					k=k.replace("=", " ");
					k=k.replace(".000000", "");
					documentxyAndAddToList(activatedScreen, blockxyIDList, k);
				}else if(l.startsWith("d")) { //TODO add else/if for MegaMan, bosses, enemies, doors, etc
					String k = l;
					k=k.replace("d", "d ");
					k=k.replace(",", " ");
					k=k.replace("\"", "");
					k=k.replace("=", " ");
					k=k.replace(".000000", "");
					documentxyAndAddToList(activatedScreen, blockxyIDList, k);

					
				}
			}
			
		}
		List<List<Integer>> complete = new ArrayList<>();		
		for(int y = 0;y<=maxY;y++) {
			List<Integer> row = new ArrayList<>();
			for(int x = 0;x<=maxX;x++) {
				row.add(7);
			}
			complete.add(row);
		}
		for(int i = 0;i<blockxyIDList.size();i++) {
			complete.get(blockxyIDList.get(i).get(1)).set(blockxyIDList.get(i).get(0), blockxyIDList.get(i).get(2));
		}
		
		System.out.println(activatedScreen.size());
		for(Point p : activatedScreen) {
			System.out.println("("+p.getX()+", "+p.getY()+")");
			for(int x = 0;x<16;x++) {
				for(int y = 0;y<14;y++) {
					if(complete.get((int) (p.getY()+y)).get((int) (p.getX()+x))==7) {
						complete.get((int) (p.getY()+y)).set((int) (p.getX()+x), 0);
					}
				}
			}
		}
		return complete;
		
		
		
	}

	private static void documentxyAndAddToList(HashSet<Point> activatedScreen, List<List<Integer>> blockxyIDList, String k) {
		List<Integer> xyID = new ArrayList<>();
		Scanner kScan = new Scanner(k);

		kScan.next();
		int xcoord = kScan.nextInt()/16;
		xyID.add(xcoord);
		int ycoord = kScan.nextInt()/16;
		xyID.add(ycoord);
		int itemID = kScan.nextInt();
		if(itemID==3) { //if ladder
			xyID.add(2); //map to ladder
		}else if(itemID==2) { //if hazard
			xyID.add(3); //map to hazard
		}else if(itemID==4) { //player
			xyID.add(11);
		}
		else { //solid block still 1
			xyID.add(itemID);
		}
		int howManySquaresX = xcoord/16;
		int howManySquaresY = ycoord/14;
		int screenX = howManySquaresX*16;
		int screenY = howManySquaresY*14;
		activatedScreen.add(new Point(screenX, screenY));
		if(xcoord>maxX) {
			maxX = xcoord+1;
		}
		if(ycoord>maxY) {
			maxY = ycoord+1;
		}
		//System.out.println(k);
		//System.out.println(l);
		kScan.close();
		blockxyIDList.add(xyID);
	}

	private static int convertMMLVTilesToInt(String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
