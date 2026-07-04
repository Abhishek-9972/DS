package DS.Stack.a01ValidParantheses;

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
                    char poppedElement = stack.pop();
                    if(ch == ')' && poppedElement!='('){return false;}
                    if(ch == '}' && poppedElement!='{'){return false;}
                    if(ch == ']' && poppedElement!='['){return false;}

                }
            }
            if(!stack.isEmpty()){
                return false;
            }
            return true;
        }
    }

