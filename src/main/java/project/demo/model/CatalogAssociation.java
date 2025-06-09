// package project.demo.model;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Entity
// @Table(name = "CatalogAssociation", schema = "dbo", catalog =
// "SouvenirShopDBUser")
// public class CatalogAssociation {

// @Id
// @GeneratedValue(strategy = GenerationType.IDENTITY)
// @Column(name = "association_id")
// private Integer associationId;

// @Column(name = "antecedent_catalog_id")
// private Integer antecedentCatalogId;

// @Column(name = "consequent_catalog_id")
// private Integer consequentCatalogId;

// @Column(name = "support")
// private Double support;

// @Column(name = "confidence")
// private Double confidence;

// @Column(name = "lift")
// private Double lift;
// }
