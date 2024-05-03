package ds.Stack.misc.ValidParantheses;

import java.util.Stack;

public class ValidParanthesis {
        public boolean isValid(String s) {
            if(s.length()==0){return true;}

            Stack<Character> stack = new Stack<>();
            for(char ch : s.toCharArray()){
                if(ch == '('|| ch == '{'|| ch =='['){
                    stack.push(ch);
                }
                else{
                    if(stack.isEmpty()){
                        return false;
                    }
                    char popedElement = stack.pop();
                    if(ch == ')' && popedElement!='('){return false;}
                    if(ch == '}' && popedElement!='{'){return false;}
                    if(ch == ']' && popedElement!='['){return false;}

                }
            }
            if(!stack.isEmpty()){
                return false;
            }
            return true;
        }
    }

