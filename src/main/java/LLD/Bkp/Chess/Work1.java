/*
package Workspace;

public class Work1 {


*/
/*Given a list of packages that need to be built , and dependencies for each package, determine a valid order in which to built the packages.
e.g


1 → {2,3}
2 → {5}
3 → {4,5,6}
4 → {3}
5 → {}
6 → {}
 determine a valid order in which to built the packages.

 for above  build order is 5 2 6 4 3 1 (may not unique)
*//*



*/
/*
Queue -> 5 6

5 2

*//*


    public class BuiltPackages{
        List<List<Integer>> graph = new ArrayList<>();
        HashMap<Integer,Integer> map = new HashMap<>();
        Queue<Integer> sourceQueue = new LinkedList<>();
        List<Integer> result = new ArrayList<>();

        int visitedNodes = 0;
        int nodes;

        public BuiltPackages(int nodes, Map<Integer,List<Integer>> map){
            this.nodes = nodes;
            for(Map.EntrySet<Integer,List<Integer>> entry: map.entrySet()){
                graph.add(entry.getKey(), new ArrayList<>());
                for(Integer individualNode : entry.getValue()){
                    addEdge(entry.getKey(), individualNode);
                }
            }
        }

        public void addEdge(int a, int b){
            graph.get(a).add(b);
            graph.get(b).add(a);

        }

        public boolean checkPackage(){
            for(Map.Entry<Integer,List<Integer>> entry: map.entrySet()){
                if(entry.getValue()==null){
                    sourceQueue.add(entry.getKey());
                }
            }

            while(!sourceQueue.isEmpty()){
                Integer node = sourceQueue.remove();
                result.add(node);
                List<Integer> neighbours = graph.get(node);
                for(Integer child : neighbours){
                    List<Integer> dependencies = map.get(child);
                    dependencies.remove(node);
                    if(dependencies.size == 0){
                        map.put(child, null);
                    }
                    map.put(child, node);
                    if(map.get(child) == null){
                        sourceQueue.add(child);
                    }
                }
            }
        }
    }



}
*/
