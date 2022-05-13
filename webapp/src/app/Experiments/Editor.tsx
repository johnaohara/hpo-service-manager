import * as React from 'react';
import {Breadcrumb, BreadcrumbItem, PageSection, Title} from '@patternfly/react-core';

const Experiments: React.FunctionComponent = () => (
  <PageSection>
      <BreadcrumbBasic></BreadcrumbBasic>

      <Title headingLevel="h1" size="lg">Experiment - Editor</Title>
      <p>This is an editor</p>
  </PageSection>
)

export const BreadcrumbBasic: React.FunctionComponent = () => (
    <Breadcrumb>
        <BreadcrumbItem to="#">experiments</BreadcrumbItem>
        <BreadcrumbItem to="#">configuration</BreadcrumbItem>
        <BreadcrumbItem to="#" isActive>
            editor
        </BreadcrumbItem>
    </Breadcrumb>
);


export { Experiments };
