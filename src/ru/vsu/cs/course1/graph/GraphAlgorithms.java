package ru.vsu.cs.course1.graph;

import java.util.*;
import java.util.function.Consumer;

public class GraphAlgorithms {

    /**
     * Поиск в глубину, реализованный рекурсивно
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void dfsRecursion(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];

        class Inner {
            void visit(Integer curr) {
                visitor.accept(curr);
                visited[curr] = true;
                for (Integer v : graph.adjacencies(curr)) {
                    if (!visited[v]) {
                        visit(v);
                    }
                }
            }
        }
        new Inner().visit(from);

    }

    public static void dfs(Graph graph, List<List<Integer>> result, List<Integer> path, int start, int finish, boolean[] visited) {
        visited[start] = true;
        path.add(start);

        if (start == finish) {
            result.add(path);
        }
        else {

            List<Integer> tempPath = new ArrayList<>(path);
            boolean[] tempVisited = visited.clone();

            for (int v : graph.adjacencies(start)) {

                if (!visited[v]) {
                    dfs(graph, result, path, v, finish, visited);

                    path = new ArrayList<>(tempPath);
                    visited = tempVisited.clone();

                }
            }
        }
    }

    public static Graph listOfWaysToGraph(List<List<Integer>> listOfWays){
        Graph graph = new AdjMatrixDigraph();
        for (int i=0;i<listOfWays.size();i++){
            for (int j=0;j<listOfWays.get(i).size()-1;j++){
                if (!graph.isAdj(listOfWays.get(i).get(j),listOfWays.get(i).get(j+1)))
                graph.addAdge(listOfWays.get(i).get(j),listOfWays.get(i).get(j+1));
            }
        }
        return graph;
    }


    /**
        Метод по пропускаю тока через граф, т.е. мы принимаем некоторую вершину from за источник тока(причем неважно, реальный ли это источник тока или нет),
        и начинаем двигаться во все стороны, превращая неориентированные ребра в ориентированные,
        что в дальнейшем позволит с помощью алгоритма поиска всех путей найти тот подграф, на котором мы будем искать сопротивление всей цепи

         Пока что граф задается в виде матрицы инцидентности/смежности, с кодом Соломы совладать не могу

         @param from - начало рассматриваемого участка
         @param to  - конец рассматриваемого участка

     Метод работает корректно(вроде, проверял несколько графов, пока проблемных случаев не выявлено), в исправлении не нуждается
     */
    public static boolean[][] bfsMy(boolean[][] graph, int from,int to) {

        //  boolean[] visited = new boolean[graph.length];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        //     visited[from] = true;

        //Заводим новый граф
        boolean[][] newVisited = new boolean[graph.length][graph.length];
        //Пока что граф несвязный, путей между вершинами нет
        for (boolean[] lArr : newVisited){
            for (boolean b : lArr){
                b=false;
            }
        }

        while (queue.size() > 0) {

            Integer curr = queue.remove();
            //Проходимся по всем вершинам, смежным с текущей
            for(int i=0;i<graph[curr].length;i++){
               //Если в новом графе еще нет пути из curr  в i и наоборот, а так же в исходном, неориентированном графе такой путь вообще есть,
                // Так же рассматривается вариант, если мы уже выходили из вершины to в текущую,
                // в таком случае возможно создание двустороннего ребра, которое после заменится на одно ребро в вершину to
                if ((!newVisited[curr][i] && !newVisited[i][curr] && graph[curr][i]) ||(i==to) &&!newVisited[curr][i]&& graph[curr][i]) {
                    queue.add(i);
                    newVisited[curr][i]=true;
                }
            }

        }

        //Исправление случая с двусторонним ребром в/из конечной вершины, заменяем на односторонее ребро
        for (int i=0;i< newVisited[to].length;i++){
            if (newVisited[i][to]) newVisited[to][i]=false;
        }

        return newVisited ;
    }



        //Метод по расчету сопротивления цепи
    public static double resistance(boolean[][] adjMatrix,int[][] resistors,int curr,double R,boolean[] visited){

        //Первое -- выясняем, последовательное ли соединение, или параллельное
        // Для этого заводим счетчик смежных вершин
        // Если есть путь между вершинами curr  и i,
        // а так же мы еще не ходили в i(по идее, для орграфа мы не можем вернуться назад, но на всякий случай добавил и эту проверку)
        // то счетчик наращивается
        int count =0;
        for (int i=0;i<adjMatrix[curr].length;i++){
            if(adjMatrix[curr][i] && !visited[i]) count++;
        }

        //Если значение счетчика больше единицы, т.е. есть более двух смежных вершин, рассматриваем вариант параллельного соединения
        if (count>1){

            //Заводим переменнные под числитель и знаменатель
            double Chislitel = 1;
            double Znamenatel = 1;
            //Проходимся по всем вершинам, смежным с текущей
            for (int i=0;i< adjMatrix[curr].length;i++){
            //Если путь есть, и мы еще не посещали вершину i, то
                if (adjMatrix[curr][i] && !visited[i]){
                    //Посещаем ее
                    visited[curr]=true;
                    //Проверки-вспомогашки
                    System.out.println(" curRes:"+resistors[curr][i]+"  cur"+(curr+1)+" to"+(i+1)+"  R"+R);
                    //Домножаем числитель на сопротивление текущего резистора, а так же рекурсивно вызываем функцию для i-й вершины
                    //Аналогично со знаменателем, но там уже складываем
                    Chislitel*=resistors[curr][i]*resistance(adjMatrix, resistors, i, R,visited);
                    Znamenatel+=resistors[curr][i]+resistance(adjMatrix, resistors, i, R,visited);

                }

            }
            //В итоге прибавляем к конечному сопротивлению цепи полученную дробь, как если бы это было последовательное соединение
            R+=Chislitel/Znamenatel;
            System.out.println("Chi"+Chislitel+" Zn"+Znamenatel);

        }
        //Случай последовательного соединения
        //Проходимся по всем смежным вершинам(вообще-то она будет одна, и ее можно было бы найти заранее, но...)
        else for (int i=0;i<adjMatrix[curr].length;i++){
            //Если есть путь в i-ю вершину, и мы ее еще не посетили
            if (adjMatrix[curr][i] && !visited[i]){
                //Посещаем
                visited[curr]=true;
                System.out.println(" curRes:"+resistors[curr][i]+"  cur"+(curr+1)+" to"+(i+1)+"  R"+R);
                //И попросту приплюсовываем к конечному сопротивлению сопр-е текущей ячейки + рекусивно вызываем для i-й вершины
                R+=resistors[curr][i]+resistance(adjMatrix, resistors, i, R, visited);
            }
        }

        return R;
    }

    //Проверяем на работоспособность
    public static void testOfNormalWork(){

        //Проверка на пропускание тока через граф
        boolean[][] graph = {
                {false,true,true,true,false,false,false,false,false},
                {true,false,false,false,true,false,false,false,false},
                {true,false,false,false,false,false,true,false,false},
                {true,false,false,false,false,true,false,false,false},
                {false,true,false,false,false,true,false,true,false},
                {false,false,false,true,true,false,false,false,false},
                {false,false,true,false,false,true,false,false,true},
                {false,false,false,false,true,false,false,false,false},
                {false,false,false,false,false,false,true,false,false}
        };

        graph= GraphAlgorithms.bfsMy(graph,0,5);
        for (boolean[] bArr : graph){
            System.out.println();
            for (boolean b : bArr){
                System.out.print(" "+b);
            }
        }


        //Проверка на поиск сопртивления
        boolean[][] newGraph = {
                {false,true,true,true,false,false,false},
                {false,false,false,false,true,false,false},
                {false,false,false,false,false,false,true},
                {false,false,false,false,false,true,false},
                {false,false,false,false,false,true,false},
                {false,false,false,false,true,false,true},
                {false,false,false,false,false,true,false}
        };
        int[][] resistors = {
                {0,5,9,6,0,0,0},
                {0,0,0,0,6,0,0},
                {0,0,0,0,0,0,6},
                {0,0,0,0,0,21,0},
                {0,0,0,0,0,8,0},
                {0,0,0,0,8,0,1},
                {0,0,0,0,0,1,0}
        };

        boolean[] visited = {false,false,false,false,false,false,false};
        System.out.println(GraphAlgorithms.resistance(newGraph,resistors,0,0,visited));
    }

    public static List<List<Integer>> allPathSourceTarget(Graph graph,int start,int finish){

        boolean[] visited = new boolean[graph.vertexCount()];
        List<List<Integer>> result = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        dfs(graph,result,path,start,finish, visited);
        return result;

    }

    public static Set<Integer> commonVertices(List<List<Integer>> list){

        Set<Integer> set = new HashSet<>();
        set.addAll(list.get(0));

        for (List<Integer> integerList : list){

            Set<Integer> result = new HashSet<>();
            result.addAll(integerList);

           set.retainAll(result);

        }

        return set;
    }


    /**
     * Поиск в глубину, реализованный с помощью стека
     * (не совсем "правильный"/классический, т.к. "в глубину" реализуется только "план" обхода, а не сам обход)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void dfs(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(from);
        visited[from] = true;
        while (!stack.empty()) {
            Integer curr = stack.pop();
            visitor.accept(curr);
            for (Integer v : graph.adjacencies(curr)) {
                if (!visited[v]) {
                    stack.push(v);
                    visited[v] = true;
                }
            }
        }
    }

    /**
     * Поиск в ширину, реализованный с помощью очереди
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    public static void bfs(Graph graph, int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[graph.vertexCount()];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        visited[from] = true;
        while (queue.size() > 0) {
            Integer curr = queue.remove();
            visitor.accept(curr);
            for (Integer v : graph.adjacencies(curr)) {
                if (!visited[v]) {
                    queue.add(v);
                    visited[v] = true;
                }
            }
        }
    }


    /**
     * Поиск в глубину в виде итератора
     * (начальная вершина также включена)
     * @param graph граф
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    public static Iterable<Integer> dfs(Graph graph, int from) {
        return new Iterable<Integer>() {
            private Stack<Integer> stack = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                stack = new Stack<>();
                stack.push(from);
                visited = new boolean[graph.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! stack.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = stack.pop();
                        for (Integer adj : graph.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                stack.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    /**
     * Поиск в ширину в виде итератора
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    public static Iterable<Integer> bfs(Graph graph, int from) {
        return new Iterable<Integer>() {
            private Queue<Integer> queue = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                queue = new LinkedList<>();
                queue.add(from);
                visited = new boolean[graph.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! queue.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = queue.remove();
                        for (Integer adj : graph.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                queue.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }
}
