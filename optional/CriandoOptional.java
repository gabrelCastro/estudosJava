import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 1) Como criar um Optional — e por que ele existe.
 *
 * Optional<T> é uma caixa que contém um valor OU está vazia. O objetivo é
 * deixar explícito na assinatura do método que o retorno pode não existir,
 * em vez de devolver null e torcer para quem chamou lembrar de checar.
 *
 * Optional NÃO existe para eliminar o null do sistema inteiro — existe para
 * ser um TIPO DE RETORNO honesto.
 */
public class CriandoOptional {

    public static void main(String[] args) {
        // --- As três formas de criar ---

        // of: o valor NÃO pode ser null. Se for, estoura NullPointerException.
        Optional<String> comValor = Optional.of("Java");
        System.out.println("of          -> " + comValor);

        // empty: a caixa vazia.
        Optional<String> vazio = Optional.empty();
        System.out.println("empty       -> " + vazio);

        // ofNullable: aceita null e devolve empty nesse caso.
        // É a ponte entre código legado (que devolve null) e Optional.
        String talvezNulo = null;
        Optional<String> daPonte = Optional.ofNullable(talvezNulo);
        System.out.println("ofNullable  -> " + daPonte);

        // --- Armadilha nº 1: Optional.of(null) ---
        try {
            Optional.of(talvezNulo);
        } catch (NullPointerException e) {
            System.out.println("\nOptional.of(null) estoura NPE na hora");
        }
        // Isso é intencional: of() é para quando você TEM CERTEZA que há valor.
        // Se estourar, o bug está em quem produziu o null, e você descobre
        // imediatamente em vez de três camadas depois.

        // Regra prática:
        //   tenho certeza que não é null?  -> Optional.of
        //   pode ser null?                 -> Optional.ofNullable
        //   quero representar "não achei"? -> Optional.empty

        // --- O problema que o Optional resolve ---
        Map<String, Integer> estoque = new HashMap<>();
        estoque.put("teclado", 5);

        // Versão null: nada na assinatura avisa que pode vir null.
        Integer qtdNull = estoque.get("mouse");
        System.out.println("\nmap.get inexistente -> " + qtdNull);
        try {
            int usado = qtdNull; // unboxing de null
            System.out.println(usado);
        } catch (NullPointerException e) {
            System.out.println("NPE só aparece na hora de usar (longe da causa)");
        }

        // Versão Optional: impossível esquecer de tratar, o compilador força.
        Optional<Integer> qtd = Optional.ofNullable(estoque.get("mouse"));
        System.out.println("com Optional        -> " + qtd.orElse(0));

        // --- Onde o próprio JDK devolve Optional ---
        // Vários métodos da API já retornam Optional; vale reconhecê-los.
        System.out.println("\nAPIs do JDK que devolvem Optional:");
        System.out.println("  Stream.findFirst : " + estoque.keySet().stream().findFirst());
        System.out.println("  Stream.max       : " + estoque.values().stream().max(Integer::compare));
        System.out.println("  Stream.reduce    : " + estoque.values().stream().reduce(Integer::sum));

        // --- Optional de primitivos ---
        // Existem OptionalInt, OptionalLong e OptionalDouble, que evitam o
        // boxing. Aparecem principalmente como retorno de IntStream & cia.
        java.util.OptionalInt maiorPrimitivo = estoque.values().stream()
                .mapToInt(Integer::intValue)
                .max();
        System.out.println("  IntStream.max    : " + maiorPrimitivo);
        System.out.println("  valor            : " + maiorPrimitivo.orElse(0));
    }
}
