package buaa.uavswarm.urbanmissionmap.activities;

import java.util.Vector;
import java.lang.Math;
import java.util.Arrays;
class Point_{
    double lng; // 经度
    double lat; // 纬度
    double alt=0; // 海拔高度
    double ht=0;  // 离地高度
    Point_(){
    	lng = 0;
    	lat = 0;
    }
    Point_(double _lng, double _lat){
    	lng = _lng;
    	lat = _lat;
    }
}

class Route{
	int type; // ��������
	Vector<Point_> pts; // ���������к��㼯��
}

class Obstacle{
	int type; //�ϰ������ͣ�����Σ�Բ��
	public Vector<Point_> pts; // �ϰ�����ı߼ʵ㼯��
	double radius; // ��ʱ����Բ���ϰ���
	boolean containPoint(double x, double y) {
		// �������Ϊ������������Χ�ڲ�
		int nCross = 0;
		if(pts.size() < 3)//�����ѱ䲻���ڻ��������
			return false;
		for (int i = 0; i < pts.size(); i++) {
			System.out.print(pts.get(i));
			double p1_x = pts.get(i).lng;
			double p1_y = pts.get(i).lat;
			double p2_x = 0;
			double p2_y = 0;
			if(i == pts.size() - 1)	
			{
				p2_x = pts.get(0).lng; // ���һ�������һ��������	
				p2_y = pts.get(0).lat; 
			}
			else
			{
				p2_x = pts.get(i+1).lng; // ���һ�������һ��������	
				p2_y = pts.get(i+1).lat; 
			}
			if ( p1_y == p2_y )   
				continue; 
			if ( y < Math.min(p1_y, p2_y) ) 
				continue; 
			if ( y >= Math.max(p1_y, p2_y) ) 
				continue;
			// �󽻵��x����				
			double isx = (double)(y - p1_y) * (double)(p2_x - p1_x) / (double)(p2_y - p1_y) + p1_x;
			// ֻͳ��p1p2��p�������ߵĽ���
			if ( isx > x )
			{
				nCross++;
			}
		}
		// ����Ϊż�������ڶ����֮��
		return (nCross % 2 == 1);
	}
}

class Params{
	int horizontal; //  ˮƽ��ȷ�ȣ�Ĭ��10�׾���
	int vertical; // ��ֱ���ȣ�Ĭ��10�׾���
	int radius;   // ת��뾶������ת��뾶���ƺ���ת��ڵ� 	
}
class TransTool{
	//��γ��תWebī����  ī����(0,0)��Ϊ����Ϊ0��γ��Ϊ0��ԭ�㣬����ƽ��Ϊƽ�棬����ΪY�ᣬ����ΪX�ᣬ��������180�ȣ���������180�ȡ�
	static public Point_ lonLat2WebMercator(Point_ lonLat)
	{
		Point_  mercator = new Point_();
	    double x = lonLat.lng *20037508.34/180;
	    double y = Math.log(Math.tan((90+lonLat.lat)*Math.PI/360))/(Math.PI/180);
	    y = y *20037508.34/180;
	    mercator.lat = y;
	    mercator.lng = x;
	    return mercator;
	}
	//Webī����ת��γ��
	static public Point_ WebMercator2lonLat(Point_ mercator)
	{
		Point_ lonLat = new Point_();
	    double x = mercator.lng/20037508.34*180;
	    double y = mercator.lat/20037508.34*180;
	    y= 180/Math.PI*(2*Math.atan(Math.exp(y*Math.PI/180))-Math.PI/2);
	    lonLat.lat = y;
	    lonLat.lng = x;
	    return lonLat;
	}
	//�ж����������Ƿ񴩹��ϰ�����rangeΪ�жϿռ䣬��������ֵΪ1�����Ϊ�ϰ���������ֵ������ϰ�����
	static public boolean crossObstacle(int st_x, int st_y, int ed_x, int ed_y, int[][] range) {
		
		for(int i = 0; i < range.length; i++) {
			for(int j = 0; j < range[i].length; j++) {
				if(1 == range[i][j]) {
					//System.out.printf("�ϰ��� ����Ϊ��%d�� * %d��\n",i, j);
					double space = 0;
				    double a, b, c;
				    a = Math.sqrt((st_x - ed_x)*(st_x - ed_x) + (st_y - ed_y)*(st_y - ed_y));
				    b = Math.sqrt((i - ed_x)*(i - ed_x) + (j - ed_y)*(j - ed_y));
				    c = Math.sqrt((st_x - i)*(st_x - i) + (st_y - j)*(st_y - j));
				    
				    if(c < 1 || b <1) {				        
				        return true;
				    }
//				    if(a < 1 && (b < 1 || c<1)) {
//				        return true;
//				    }    
				    if(c*c >= a*a + b*b){
				        break;
				    }
				    if(b*b >= a*a + c*c){
				    	break;
				    }
				    boolean isC = (c*c >= a*a + b*b);
				    boolean isB = (b*b >= a*a + c*c);
				    if((!isC) && (!isB)) {
				    	double p = (a + b + c) / 2;// ���ܳ�
					    double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// ���׹�ʽ�����
					    space = 2 * s / a;// ���ص㵽�ߵľ��루���������������ʽ��ߣ�
					    if(space < Math.sqrt(2.0)/2) {
					    	return true;
					    }
				    }	    
				} 
			}
		}
		return false;
	}
}

public class RoutePlan {
	
	static public Route getRoute(Point_ startPt, Point_ endPt, Vector<Obstacle> Obtls, Params para) // ������㡢�յ㼰�ϰ��Ｏ�ϣ��������պ��߽��
	{
		Route result = new Route();
		// 0. ����׼����������ͳһ����XYZֱ������ϵ�Ŀռ���
		Point_ startPt_xy = TransTool.lonLat2WebMercator(startPt);
		Point_ endPt_xy = TransTool.lonLat2WebMercator(endPt);
		System.out.printf("�������Ϊ��%f * %f\n",startPt_xy.lng, startPt_xy.lat);
		System.out.printf("�յ�����Ϊ��%f * %f\n",endPt_xy.lng, endPt_xy.lat);
		Vector<Obstacle> Obtls_xy = new Vector<Obstacle>();
		for (Obstacle obstl : Obtls) {
			Obstacle obstl_xy = new Obstacle();
			obstl_xy.pts = new Vector<Point_>();
			for (Point_ pt : obstl.pts) {
				Point_ pt_xy = TransTool.lonLat2WebMercator(pt);
				obstl_xy.pts.add(pt_xy);
			}
			Obtls_xy.add(obstl_xy);	
		}
        for(Obstacle obtltmp : Obtls_xy) {
        	System.out.printf("ת���� �ϰ��� �߽������ %d\n", obtltmp.pts.size());
        	for(Point_ ptmp:obtltmp.pts) {
        		System.out.printf("ת���� �ϰ��� �߽��λ�� %f %f\n", ptmp.lng,ptmp.lat);
        	}
        }
		System.out.println("0. ����׼����������ͳһ����XYZֱ������ϵ�Ŀռ� ���");
		// 1��  ��������յ㣬�����㷨������Χ����ʼ�������ռ�
		// x�᣺lng    y�᣺lat ????
		double midX = (startPt_xy.lng + endPt_xy.lng)/2;
		double midY = (startPt_xy.lat + endPt_xy.lat)/2;
		double dis = Math.sqrt((startPt_xy.lng - endPt_xy.lng)*(startPt_xy.lng - endPt_xy.lng) + (startPt_xy.lat - endPt_xy.lat)*(startPt_xy.lat - endPt_xy.lat));
		dis = 1.4*dis/2; // ���߹滮����İ뾶��
		double minX = midX - dis;
		double minY = midY - dis;
		double maxX = midX + dis;
		double maxY = midY + dis;
		int gridSize = (para.horizontal >= 10) ? para.horizontal : 10;
		int horn = (int)(2*dis/gridSize); // ����Ҫ�γ���һ��N*N�ľ���
		System.out.printf("�����ĵ�ͼ�ߴ�Ϊ��%d*%d\n", horn, horn);
		System.out.printf("�����ĵ�ͼ��׼������Ϊ��%f*%f\n", minX, maxY);
		int map[][] = new int[horn][horn];
		
		// ��ʼ����� �յ� �ֱ����㷨�����ռ�Ķ�λ��-> ת��Ϊ��������б���ʱ���ٴα仯��ԭ�㲻�������½ǣ��������Ͻǡ�
		int st_x = (int)(startPt_xy.lng - minX)/gridSize;
		int st_y = (int)(maxY - startPt_xy.lat)/gridSize;
		int ed_x = (int)(endPt_xy.lng - minX)/gridSize;
		int ed_y = (int)(maxY - endPt_xy.lat)/gridSize;
		System.out.printf("��� �յ�����Ϊ��%d %d -> %d %d\n",st_y, st_x, ed_y, ed_x);
		map[st_y -1][st_x - 1] = -1;
		map[ed_y - 1][ed_x - 1] = -9;
		for (int i=0;i<horn;i++)
            System.out.println (Arrays.toString (map[i]));
		System.out.println("1��  ��������յ㣬�����㷨������Χ����ʼ�������ռ� ���");
		// 2. �����ϰ����������һ�������ռ�
		//int mapObtl[][] = new int[horn][horn]; ���������Ҫ���о���Ļ���������Ҫһ���м����������ɡ�
		for (Obstacle obstl : Obtls_xy) { // ��ʼѭ������ �ϰ�������Ϣ����һ�������ռ�
			// �˴������ɸ����ϰ��ﷶΧ�������ռ䷶Χ��ȡ�����������Ż�����Ч�ʣ����ⲻ��Ҫ��ѭ�����������ٽ����Ż�
			for(Point_ pt1 : obstl.pts) {
				System.out.printf("�ϰ����� �߽����Ϣ%f %f\n", pt1.lng, pt1.lat);
			}
            for(int m = 0; m < horn; m++) // ��
            {
                for(int n = 0; n < horn; n++) // ��
                {
                	double m_y = maxY - m*gridSize; // 
                	double n_x = minX + n*gridSize; // 
                	if(m<5&&n<5)
                		System.out.printf("��ͼλ��%d��%d�� ->��Ӧ���� %f %f\n", m, n, n_x, m_y);
                	boolean inPolygon = obstl.containPoint(n_x, m_y);
                    if(inPolygon) //�жϵ��Ƿ��ڽ������У��ڽ������ڵ�ֱ����Ϊ1�����ڵĲ��ı�ԭ��ֵ  &&(map[m][n] != -1)
                    {
                    	//mapObtl[m][n]= 1;
                    	map[m][n]= 1;
                    }
                }
            }   
		}
		for (int i=0;i<horn;i++)
            System.out.println (Arrays.toString (map[i]));
		// �˴��������¾�����������Ʒɽ�������׼ȷ�ȡ�
		//arma::mat cnn = arma::ones(3, 3);    //3*3 ��ȫ1��
        //result = arma::conv2(result, cnn, "same");//�ȶ������ϰ���Ϣ�������ţ���֤��·���
        //result.elem(find(result >= 1)) -= (result.elem(find(result >= 1)) - 1);	
		System.out.println("2. �����ϰ����������һ�������ռ� ���");
		// 3. ���ø��ʵ�ͼ�����������ռ��Ż���
		System.out.println("3. ���ø��ʵ�ͼ�����������ռ��Ż� ���������ƣ���ǰ�汾δʵ��");
		// 4. ����A*�����㷨��������·����
    	Puzzle aStar = new Puzzle(horn, horn, map, st_y,st_x,ed_y,ed_x);
    	System.out.println("��ʼ���Թ����£�������&������㣬@�����յ㣬#�����ϰ���");
    	aStar.printMaze();    //�����ʼ�����Թ�
    	System.out.println();
    	System.out.println("��A*�㷨Ѱ··�����£�");
    	aStar.Astar();
		System.out.println("4. ����A*�����㷨��������·�� ��ϣ����ۺ�������δϸ������������");
		// 5. ����A*�㷨�ٴ�ƽ����������
		Node[] resultRoute = aStar.getResult();
		System.out.printf("��A*�㷨Ѱ··��������Ϊ��%d\n", resultRoute.length);
		for(int i = 0; i < resultRoute.length; i++) {
			System.out.printf("��A*�㷨Ѱ··��������Ϊ��%d:%d %d\n", i,resultRoute[i].x, resultRoute[i].y);
		}
		Vector<Node > smoothResult = new Vector<Node>();
		int target_x = resultRoute[resultRoute.length - 1].x;
		int target_y = resultRoute[resultRoute.length - 1].y;
		int target_id = resultRoute.length - 1;
		smoothResult.add(resultRoute[resultRoute.length - 1]);
		
		for(int i = 0; i < resultRoute.length - 1; i++) {
			int[][] stopMap = new int[horn][horn];
			stopMap[2][2] = 1;
			stopMap[3][3] = 1;
			stopMap[4][6] = 1;
			//�ж���ʼ���Ƿ���յ����߿���
			if(i == target_id - 1) {
				//����˴������������������������ƽ����
				smoothResult.add(resultRoute[i]);
				//����Ŀ��㣻
				target_x = resultRoute[i].x;
				target_y = resultRoute[i].y;
				target_id = i;
				if(0 == i) 
				{
					break;
				}
				//
				i = -1;			
			}
			else 
			{
				//System.out.printf("a���ж��Ƿ�Խ��������%d %d\n",resultRoute[i].x, resultRoute[i].y);
				boolean isCross = TransTool.crossObstacle(resultRoute[i].x, resultRoute[i].y, target_x, target_y, map); // stopMap
				//System.out.printf("���ж��Ƿ�Խ��������%d %d -> %d %d %b\n", resultRoute[i].x, resultRoute[i].y, target_x,target_y, isCross);
				if(!isCross) {
					//����˴������������������������ƽ����
					smoothResult.add(resultRoute[i]);
					//����Ŀ��㣻
					target_x = resultRoute[i].x;
					target_y = resultRoute[i].y;
					target_id = i;
					if(0 == i) 
					{
						break;
					}
					else 
					{
						i = -1; // 
						continue;
					}
				}
			}
		}
		System.out.printf("ƽ��·���󣬺�������Ϊ��%d\n", smoothResult.size());
		System.out.println("5. ����A*�㷨�ٴ�ƽ�������� ��� �����Ƿ�Խ�����������в����ƣ����ܴ���BUG");
		// 6. ת��Ϊ���������ʽ������ϵ��
		result.pts = new Vector<Point_>();
		result.pts.add(startPt);
		for(Node nd:smoothResult) {
			double x = nd.y*gridSize + minX;
			double y = maxY - nd.x*gridSize;
			Point_ pt_tmp = new Point_(x, y);
			Point_ pt_rst = TransTool.WebMercator2lonLat(pt_tmp);
			result.pts.add(pt_rst);
		}
		result.pts.add(endPt);
		//
		System.out.printf("6. ת��Ϊ���������ʽ������ϵ������Ϊ%d\n",result.pts.size());
		for(Point_ pt:result.pts) {
			System.out.printf("����·����� ����%f γ��%f\n", pt.lng, pt.lat);
		}
		return result;	
	}
}
