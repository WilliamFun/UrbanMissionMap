package buaa.uavswarm.urbanmissionmap.activities;

import java.util.Stack;


class Node{
    boolean attainability; //�ڵ��Ƿ�ɴﵽ
    int x,y; //�ڵ��λ����Ϣ
    int direction = 1;
    int score;
    String id = " ";
    //Node leftNode,rightNode,upNode,downNode;
    //Node fatherNode;
}
public class Puzzle {
    Node[][] maze = null; //�Թ�����
    Node startNode,endNode; //��ʼ�㣬������
    int row;
    int col;
    Node[] result = null;
    public Puzzle(){
        //��ʼ���Թ�����~��ָ����ʼ��ͽ�����
        maze = new Node[7][9];
        row = 7;
        col = 9;
        for(int i=0;i<7;i++)
            for(int j=0;j<9;j++){
                maze[i][j] = new Node();

                maze[i][j].attainability = true;
                maze[i][j].x = i;
                maze[i][j].y = j;
        }
        startNode = maze[3][2];
        startNode.id = "&";
        endNode = maze[3][6];
        endNode.id = "@";
        for(int i=0;i<7;i++){
            maze[i][0].attainability = false;
            maze[i][8].attainability = false;
        }
        for(int j=0;j<9;j++){
            maze[0][j].attainability = false;
            maze[6][j].attainability = false;
        }
        maze[4][3].attainability = false;
        maze[2][4].attainability = false;
        maze[3][4].attainability = false;
        maze[4][4].attainability = false;
    }
    public Puzzle(int row, int col, int data[][], int st_r, int st_c, int ed_r, int ed_c) {
    	this.row = row;
    	this.col = col;
    	maze = new Node[row][col];
        for(int i = 0; i < row; i++) {
            for(int j = 0; j < col; j++){
                maze[i][j] = new Node();
                maze[i][j].attainability = true;
                maze[i][j].x = i;
                maze[i][j].y = j;
            }
        }
        //
        startNode = maze[st_r][st_c];
        startNode.id = "&";
        endNode = maze[ed_r][ed_c];
        endNode.id = "@";
        //
        for(int i = 0; i < row; i++) {
            for(int j = 0; j < col; j++){
            	if(1 == data[i][j])
                maze[i][j].attainability = false;
            }
        }       
    }
    public void printMaze(){

        for(int i=0;i<row;i++){
            for(int j=0;j<col;j++){

                if(maze[i][j].attainability)
                    if(maze[i][j] == startNode)
                        System.out.print("& ");
                    else if(maze[i][j] == endNode)
                        System.out.print("@ ");
                    else System.out.print(maze[i][j].id + " ");

                else
                    if(!maze[i][j].attainability) //i==0 || i==6 || j==0 ||j==8 || (i==4&&j==3) ||(i==2&&j==4) ||(i==3&&j==4) ||(i==4&&j==4)
                    //if(!maze[i][j].attainability) //i==0 || i==6 || j==0 ||j==8 || (i==4&&j==3) ||(i==2&&j==4) ||(i==3&&j==4) ||(i==4&&j==4)
                    	System.out.print("# ");
                    else System.out.print(maze[i][j].id + " ");
            }
            System.out.println();
        }
    }

    public void getPath(){
        Stack s = new Stack();
        Node curpos = startNode;
        //int curstep = 1;
        do{
            if(curpos.attainability){
                curpos.attainability = false;
                if(curpos!=startNode && curpos!=endNode)
                    curpos.id = "%";
                s.push(curpos);
                if(curpos == endNode) printMaze();
                Node tmp = nextPos(curpos,1);
                curpos = maze[tmp.x][tmp.y];
            //    curpos.id++;
            //    curstep++;
            }
            else{
                Node e = (Node)s.pop();
                while(e.direction == 4 && !s.empty()){
                    e.attainability = false;
                    e = (Node)s.pop();
                }
                if(e.direction < 4){
                    e.direction++;
                    s.push(e);
                    Node tmp = nextPos(e,e.direction);
                    curpos = maze[tmp.x][tmp.y];
                }
            }
        }while(!s.empty());
    }

    private Node nextPos(Node pos, int i){
        //������������˳��
        Node tmp = new Node();
        tmp.direction = pos.direction;
        tmp.attainability = pos.attainability;
        int x = pos.x;
        int y = pos.y;
        if(i == 2 && x<maze.length-1){     //��
            x = x+1;
            tmp.y = pos.y;
            tmp.x = x;
        }
        else if(i == 4 && x != 0){     //��
            x = x-1;
            tmp.y = pos.y;
            tmp.x = x;
        }
        else if(i == 1 && y<maze[0].length-1){    //��
            y = y+1;
            tmp.x = pos.x;
            tmp.y = y;
        }
        else if(i == 3 && y != 0){    //��
            y = y - 1;
            tmp.x = pos.x;
            tmp.y = y;
        }
        return tmp;
    }

    public void Astar(){
        getScore();

        Stack open = new Stack(); //��ʼ�����б�
        open.push(startNode);

        Stack close = new Stack(); //��ʼ�ر��б�
        while(open.size()!=0){
            Node x = (Node)open.pop();
            if(x == endNode)
                break;
            Node[] t = null;
            if(x.attainability){
                t = getNear(x);

                for(int i=0;i<t.length;i++){
                    if(t[i] != null){
                        if(open.search(t[i]) == -1 && close.search(t[i]) == -1)
                            open.push(t[i]);
                        else if(open.search(t[i]) != -1){
                            sort(open);
                        }
                        else{
                            if(t[i].score < ((Node)close.peek()).score){
                                sort(close);
                                open.push(close.pop());
                            }
                        }
                    }
                }
                close.push(x);
                sort(open);
            }
        }
        int a = close.size();
        if(a>1) {
        	result = new Node[a];
        }
        for(int i=0;i<a;i++){
            Node tmp = (Node)close.pop();
            tmp.id = "%";
            maze[tmp.x][tmp.y] = tmp;
            result[i] = tmp;
        }
        printMaze();
    }


    private void sort(Stack s) {
        // ��һ��ջ���Ԫ�ذ�score����ֵ���ٵķ��������档
        Node[] t = new Node[s.size()];
        Node tmp = null;
        int x = s.size();
        for(int i=0;i<x;i++){
            t[i] = (Node)s.pop();
        }
        for(int i=0;i<t.length-1;i++)
            for(int j=i+1;j<t.length;j++){
                if(t[i].score < t[j].score){
                    tmp = t[i];
                    t[i] = t[j];
                    t[j] = tmp;
                }
            }
        for(int i=0;i<t.length;i++)
            s.push(t[i]);
    }

    private void getScore(){
        for(int i=0;i<maze.length;i++)
            for(int j=0;j<maze[i].length;j++){
                //maze[i][j].score = Math.abs(endNode.x - maze[i][j].x) + Math.abs(endNode.y - maze[i][j].y);
                maze[i][j].score = (int)Math.sqrt( (endNode.x - maze[i][j].x)*(endNode.x - maze[i][j].x) + (endNode.y - maze[i][j].y)*(endNode.y - maze[i][j].y));
            }
    }
    private Node[] getNear(Node e){

        Node leftNode = (e.y!=0 ? maze[e.x][e.y-1] : null);
        Node rightNode = (e.y!=maze[0].length-1 ? maze[e.x][e.y+1] : null);
        Node upNode = (e.x!=0 ? maze[e.x-1][e.y] : null);
        Node downNode = (e.x!=maze.length-1 ? maze[e.x+1][e.y] : null);
        // ����б�߷���ĵ㣬ÿ�����������8���ٽ��
        Node leftUpNode = ((e.y!=0&&e.x!=0) ? maze[e.x-1][e.y-1] : null);
        Node rightUpNode = ((e.y!=maze[0].length-1&&e.x!=0) ? maze[e.x-1][e.y+1] : null);
        Node leftDownNode = ((e.y!=0&&e.x!=maze.length-1) ? maze[e.x+1][e.y-1] : null);
        Node rightDownNode = ((e.y!=maze[0].length-1&&e.x!=maze.length-1) ? maze[e.x+1][e.y+1] : null);
      
        Node[] t =  {leftNode,rightNode,upNode,downNode,leftUpNode,rightUpNode,leftDownNode,rightDownNode};

        return t;
    }
    public Node[] getResult(){
    	return result;
    }
    public static void main(String[] args){
    	
        Puzzle p = new Puzzle();
        System.out.println("��ʼ���Թ����£�������&������㣬@�����յ㣬#�����ϰ���");
        p.printMaze();    //�����ʼ�����Թ�
        System.out.println();
        System.out.println("�򵥵�һ��Ѱ··�����£���������������˳�����Ѱ·��");
        p.getPath();        //�����Ѱ����ʼ�㵽�������·��

        p = new Puzzle();
        System.out.println();
        System.out.println("��A*�㷨Ѱ··�����£�");
        p.Astar();


    }
}
//public class Puzzle {
//
//}
