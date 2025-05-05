// package project.demo.service.implement;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import project.demo.model.Catalog;
// import project.demo.model.CatalogAssociation;
// import project.demo.model.Order;
// import project.demo.model.OrderDetail;
// import project.demo.repository.*;
// import project.demo.service.IRecommendationService;

// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// public class RecommendationServiceImpl implements IRecommendationService {

// private final OrderRepository orderRepository;
// private final OrderDetailRepository orderDetailRepository;
// private final ProductRepository productRepository;
// private final CatalogRepository catalogRepository;
// private final CatalogAssociationRepository catalogAssociationRepository;

// public RecommendationServiceImpl(
// OrderRepository orderRepository,
// OrderDetailRepository orderDetailRepository,
// ProductRepository productRepository,
// CatalogRepository catalogRepository,
// CatalogAssociationRepository catalogAssociationRepository) {
// this.orderRepository = orderRepository;
// this.orderDetailRepository = orderDetailRepository;
// this.productRepository = productRepository;
// this.catalogRepository = catalogRepository;
// this.catalogAssociationRepository = catalogAssociationRepository;
// }

// @Override
// @Transactional
// public void generateCatalogAssociations(double minSupport, double
// minConfidence) {
// // Get all orders
// List<Order> orders = orderRepository.findAll();

// // Create transactions (sets of catalog IDs for each order)
// List<Set<Integer>> transactions = new ArrayList<>();
// for (Order order : orders) {
// Set<Integer> transaction =
// orderDetailRepository.findByOrderId(order.getOrderId())
// .stream()
// .map(OrderDetail::getProductId)
// .map(productId -> productRepository.findById(productId).get().getCatalogId())
// .collect(Collectors.toSet());
// transactions.add(transaction);
// }

// // Generate frequent itemsets
// Map<Set<Integer>, Integer> frequentItemsets =
// generateFrequentItemsets(transactions, minSupport);

// // Generate association rules
// List<CatalogAssociation> associations =
// generateAssociationRules(frequentItemsets, transactions.size(),
// minConfidence);

// // Save associations to database
// catalogAssociationRepository.deleteAll();
// catalogAssociationRepository.saveAll(associations);
// }

// @Override
// public List<Catalog> getRecommendedCatalogs(Integer catalogId, int limit) {
// return
// catalogAssociationRepository.findByAntecedentCatalogIdOrderByConfidenceDesc(catalogId)
// .stream()
// .limit(limit)
// .map(CatalogAssociation::getConsequentCatalogId)
// .map(id -> catalogRepository.findById(id).orElse(null))
// .filter(Objects::nonNull)
// .collect(Collectors.toList());
// }

// @Override
// public List<CatalogAssociation> getCatalogAssociations(double minConfidence)
// {
// return
// catalogAssociationRepository.findByConfidenceGreaterThanOrderByConfidenceDesc(minConfidence);
// }

// private Map<Set<Integer>, Integer>
// generateFrequentItemsets(List<Set<Integer>> transactions, double minSupport)
// {
// Map<Set<Integer>, Integer> frequentItemsets = new HashMap<>();

// // Generate single-item frequent itemsets
// Map<Integer, Integer> itemCounts = new HashMap<>();
// for (Set<Integer> transaction : transactions) {
// for (Integer item : transaction) {
// itemCounts.merge(item, 1, Integer::sum);
// }
// }

// // Filter by minimum support
// int minCount = (int) (minSupport * transactions.size());
// Set<Integer> frequentItems = itemCounts.entrySet().stream()
// .filter(e -> e.getValue() >= minCount)
// .map(Map.Entry::getKey)
// .collect(Collectors.toSet());

// // Add single-item frequent itemsets
// for (Integer item : frequentItems) {
// frequentItemsets.put(Collections.singleton(item), itemCounts.get(item));
// }

// // Generate k-item frequent itemsets
// int k = 2;
// while (!frequentItems.isEmpty()) {
// Set<Set<Integer>> candidates = generateCandidates(frequentItems, k);
// Map<Set<Integer>, Integer> candidateCounts = new HashMap<>();

// // Count occurrences of candidate itemsets
// for (Set<Integer> transaction : transactions) {
// for (Set<Integer> candidate : candidates) {
// if (transaction.containsAll(candidate)) {
// candidateCounts.merge(candidate, 1, Integer::sum);
// }
// }
// }

// // Filter by minimum support
// frequentItemsets.putAll(
// candidateCounts.entrySet().stream()
// .filter(e -> e.getValue() >= minCount)
// .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
// );

// frequentItems = candidateCounts.entrySet().stream()
// .filter(e -> e.getValue() >= minCount)
// .map(Map.Entry::getKey)
// .flatMap(Set::stream)
// .collect(Collectors.toSet());

// k++;
// }

// return frequentItemsets;
// }

// private Set<Set<Integer>> generateCandidates(Set<Integer> items, int k) {
// Set<Set<Integer>> candidates = new HashSet<>();
// List<Integer> itemList = new ArrayList<>(items);

// // Generate k-item candidates from (k-1)-item frequent itemsets
// for (int i = 0; i < itemList.size(); i++) {
// for (int j = i + 1; j < itemList.size(); j++) {
// Set<Integer> candidate = new HashSet<>();
// candidate.add(itemList.get(i));
// candidate.add(itemList.get(j));
// if (candidate.size() == k) {
// candidates.add(candidate);
// }
// }
// }

// return candidates;
// }

// private List<CatalogAssociation> generateAssociationRules(
// Map<Set<Integer>, Integer> frequentItemsets,
// int totalTransactions,
// double minConfidence) {

// List<CatalogAssociation> associations = new ArrayList<>();

// for (Map.Entry<Set<Integer>, Integer> entry : frequentItemsets.entrySet()) {
// Set<Integer> itemset = entry.getKey();
// if (itemset.size() < 2) continue;

// // Generate all possible antecedent-consequent pairs
// for (Integer item : itemset) {
// Set<Integer> antecedent = Collections.singleton(item);
// Set<Integer> consequent = new HashSet<>(itemset);
// consequent.remove(item);

// // Calculate confidence and lift
// double confidence = (double) entry.getValue() /
// frequentItemsets.getOrDefault(antecedent, 0);
// if (confidence >= minConfidence) {
// double support = (double) entry.getValue() / totalTransactions;
// double consequentSupport = (double) frequentItemsets.getOrDefault(consequent,
// 0) / totalTransactions;
// double lift = confidence / consequentSupport;

// // Create association rule
// for (Integer consequentItem : consequent) {
// CatalogAssociation association = new CatalogAssociation();
// association.setAntecedentCatalogId(item);
// association.setConsequentCatalogId(consequentItem);
// association.setSupport(support);
// association.setConfidence(confidence);
// association.setLift(lift);
// associations.add(association);
// }
// }
// }
// }

// return associations;
// }
// }