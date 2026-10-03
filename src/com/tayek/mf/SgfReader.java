package com.tayek.mf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SgfReader {
    private static final Pattern property = Pattern.compile("([A-Z]+)\\[((?:\\\\.|[^]])*)]");
    private SgfReader() { }

    public static SgfGame read(Path path) throws IOException {
        String sgf=Files.readString(path);
        int size=intProperty(sgf,"SZ",19);
        String black=stringProperty(sgf,"PB","Black"),white=stringProperty(sgf,"PW","White"),name=stringProperty(sgf,"GN",path.getFileName().toString());
        Parser parser=new Parser(sgf,size);
        GameNode root=parser.parse();
        List<Move> main=new ArrayList<>();
        GameNode n=root;
        while(n!=null&&!n.children().isEmpty()){n=n.children().get(0);if(n.move()!=null)main.add(n.move());}
        return new SgfGame(size,black,white,name,List.copyOf(main),root);
    }

    private static final class Parser {
        private final String s; private final int size; private int i;
        Parser(String s,int size){this.s=s;this.size=size;}
        GameNode parse(){GameNode root=new GameNode(null,null);skip();if(i<s.length()&&s.charAt(i)=='(')parseTree(root);return root;}
        private void parseTree(GameNode parent){i++;GameNode current=parent;skip();while(i<s.length()&&s.charAt(i)==';'){Move m=parseNode();if(m!=null){GameNode child=new GameNode(m,current);current.addChild(child);current=child;}skip();}while(i<s.length()&&s.charAt(i)=='('){parseTree(current);skip();}if(i<s.length()&&s.charAt(i)==')')i++;}
        private Move parseNode(){i++;Move move=null;while(i<s.length()){skip();if(i>=s.length())break;char c=s.charAt(i);if(c==';'||c=='('||c==')')break;if(!Character.isUpperCase(c)){i++;continue;}int start=i;while(i<s.length()&&Character.isUpperCase(s.charAt(i)))i++;String id=s.substring(start,i);skip();while(i<s.length()&&s.charAt(i)=='['){String value=value();if((id.equals("B")||id.equals("W"))&&move==null){Stone stone=id.equals("B")?Stone.BLACK:Stone.WHITE;if(value.isEmpty())move=new Move(-1,-1,stone);else if(value.length()>=2){int x=value.charAt(0)-'a',y=value.charAt(1)-'a';if(x>=0&&x<size&&y>=0&&y<size)move=new Move(x,y,stone);}}skip();}}return move;}
        private String value(){i++;StringBuilder b=new StringBuilder();boolean escaped=false;while(i<s.length()){char c=s.charAt(i++);if(escaped){b.append(c);escaped=false;}else if(c=='\\')escaped=true;else if(c==']')break;else b.append(c);}return b.toString();}
        private void skip(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}
    }

    private static String stringProperty(String sgf,String id,String fallback){Matcher m=Pattern.compile("(?:^|[;\\s])"+id+"\\[((?:\\\\.|[^]])*)]").matcher(sgf);return m.find()?unescape(m.group(1)):fallback;}
    private static int intProperty(String sgf,String id,int fallback){try{return Integer.parseInt(stringProperty(sgf,id,Integer.toString(fallback)));}catch(NumberFormatException e){return fallback;}}
    private static String unescape(String s){return s.replace("\\]","]").replace("\\\\","\\");}
}
