import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 1) A diferença entre map e flatMap.
 *
 * map     -> transforma 1 elemento em 1 elemento.  Stream<A> -> Stream<B>
 * flatMap -> transforma 1 elemento em N elementos (um Stream) e "achata"
 *            todos esses streams em um só.         Stream<A> -> Stream<B>
 *
 * A pegadinha: se você usar map com uma função que devolve uma lista,
 * o resultado é Stream<List<B>> (aninhado). O flatMap resolve isso.
 */
public class MapVsFlatMap {

    public static void main(String[] args) {
        List<List<String>> nomesPorTurma = List.of(
                List.of("Ana", "Bruno"),
                List.of("Carla", "Diego", "Eva"),
                List.of("Fabio")
        );

        // --- com map: continua aninhado ---
        // A função (turma -> turma) devolve uma List, então cada elemento do
        // stream vira uma List. Resultado: List<List<String>>.
        List<List<String>> comMap = nomesPorTurma.stream()
                .map(turma -> turma)
                .collect(Collectors.toList());

        System.out.println("map     -> " + comMap);
        // map     -> [[Ana, Bruno], [Carla, Diego, Eva], [Fabio]]

        // --- com flatMap: achatado ---
        // A função devolve um Stream<String>. O flatMap concatena os 3 streams
        // gerados em um único Stream<String>.
        List<String> comFlatMap = nomesPorTurma.stream()
                .flatMap(turma -> turma.stream())
                .collect(Collectors.toList());

        System.out.println("flatMap -> " + comFlatMap);
        // flatMap -> [Ana, Bruno, Carla, Diego, Eva, Fabio]

        // Depois de achatar, dá pra encadear normalmente:
        String maiusculas = nomesPorTurma.stream()
                .flatMap(List::stream)          // method reference equivalente a turma -> turma.stream()
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.joining(", "));

        System.out.println("achatado + tratado -> " + maiusculas);

        // --- Detalhe importante: flatMap pode devolver 0 elementos ---
        // Se a função devolver Stream.empty(), o elemento simplesmente some.
        // Isso torna o flatMap um "filter + map" ao mesmo tempo.
        List<String> soComA = nomesPorTurma.stream()
                .flatMap(List::stream)
                .flatMap(nome -> nome.contains("a") ? Stream.of(nome) : Stream.empty())
                .toList();

        System.out.println("flatMap filtrando  -> " + soComA);

        // --- E pode devolver mais elementos do que entrou ---
        List<String> duplicados = Stream.of("x", "y")
                .flatMap(s -> Stream.of(s, s))
                .toList();

        System.out.println("flatMap duplicando -> " + duplicados); // [x, x, y, y]
    }
}
